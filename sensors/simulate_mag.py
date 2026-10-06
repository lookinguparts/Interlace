#!/usr/bin/env python3
"""Simulate an Interlace tower magnetometer and stream it as OSC.

Stands in for the ESP32 + LIS3MDL of Interlace_Arduino_Raw.ino so the OSCPlay
recording and calibration pipeline can be exercised without the installation.
Sends the same message the sketch does: three floats of raw sensor counts.

Each tower is a ~6 ft ring that people turn. Elastic bands pull it back to a
centre rest position, and welded stops limit it to 135 degrees either side of
centre, so 270 degrees of travel in total.

Two modes:

  play       People walking up and turning the ring, letting go, and the bands
             pulling it back through centre. The default.

  calibrate  The sweep from OSCPlay's docs/interlace_calibration_guide.md: rest
             against the 0 degree stop, then hold still at every 10 degree mark
             through to the 270 degree stop. Record this in OSCPlay and run
             Tools > Sensor Calibration on it.

Examples:
  python3 simulate_mag.py --mag 1 --host 192.168.1.110
  python3 simulate_mag.py --mag 2 --mode calibrate --check
  python3 simulate_mag.py --mag 3 --mode calibrate

Sensor model: the ring turns about a near-vertical axis, so the horizontal part
of the Earth's field traces an arc in sensor XY. The surrounding steel offsets
that arc from the origin (hard iron) and squashes it into an ellipse (soft
iron), and the sensor board is mounted at a tilt, which couples rotation into Z.
The defaults were fitted to a real recording (OSCPlay's calibration1.csv).
"""

import argparse
import csv
import math
import random
import socket
import struct
import sys
import time

# Travel between the welded stops, and the spacing of the calibration marks.
ARC_DEGREES = 270.0
MARK_SPACING_DEGREES = 10.0

# Per-tower sensor distortion. Each tower sits in different steel and so has its
# own hard iron offset, soft iron ellipse and mounting tilt.
#   centre        hard iron offset, in counts
#   radius        horizontal field amplitude, in counts
#   xyrat         soft iron ellipse axis ratio
#   ellipse_deg   rotation of the ellipse's major axis
#   tilt_deg      sensor mount tilt, which swings Z as the ring turns
#   heading_deg   compass heading of the sensor at the 0 degree stop
PROFILES = {
    1: dict(centre=(-2574.0, 3051.0, -3600.0), radius=1938.0, xyrat=1.02,
            ellipse_deg=8.0, tilt_deg=42.0, heading_deg=20.0),
    2: dict(centre=(1820.0, -2460.0, 2900.0), radius=2010.0, xyrat=1.09,
            ellipse_deg=-15.0, tilt_deg=38.0, heading_deg=95.0),
    3: dict(centre=(-1581.0, 1522.0, -2750.0), radius=1870.0, xyrat=1.02,
            ellipse_deg=22.0, tilt_deg=45.0, heading_deg=250.0),
}

# Per-axis Gaussian noise in counts. The lower quartile of the consecutive
# differences in calibration1.csv implies a sigma of about 29.
DEFAULT_NOISE = 28.0


# --- OSC ------------------------------------------------------------------

def osc_string(s):
    """Encode an OSC string: null-terminated, padded to a multiple of 4 bytes."""
    b = s.encode("utf-8") + b"\0"
    return b + b"\0" * (-len(b) % 4)


def osc_message(address, floats):
    return (osc_string(address)
            + osc_string("," + "f" * len(floats))
            + b"".join(struct.pack(">f", v) for v in floats))


# --- Sensor ---------------------------------------------------------------

class Sensor:
    """Turns a ring angle into the counts the magnetometer would report."""

    def __init__(self, profile, noise, rng):
        self.cx, self.cy, self.cz = profile["centre"]
        self.radius = profile["radius"]
        self.xyrat = profile["xyrat"]
        self.psi = math.radians(profile["ellipse_deg"])
        self.tilt = math.radians(profile["tilt_deg"])
        self.heading = math.radians(profile["heading_deg"])
        self.noise = noise
        self.rng = rng

    def read(self, mark_deg):
        """mark_deg is degrees from the 0 degree stop, so 0 to 270."""
        a = self.heading + math.radians(mark_deg)
        # Ideal circle in the sensor's XY plane, squashed into the soft iron
        # ellipse and rotated onto its measured axes.
        u = self.radius * math.cos(a)
        v = self.radius * math.sin(a) / self.xyrat
        x = self.cx + u * math.cos(self.psi) - v * math.sin(self.psi)
        y = self.cy + u * math.sin(self.psi) + v * math.cos(self.psi)
        # A tilted mount leaks the rotation into Z.
        z = self.cz + self.radius * math.sin(self.tilt) * math.sin(a)
        g = self.rng.gauss
        return (x + g(0, self.noise), y + g(0, self.noise), z + g(0, self.noise))


# --- Motion ---------------------------------------------------------------

def calibration_angles(rate, hold_s, move_s, arc, step):
    """Yield mark angles for the calibration sweep, one per sample."""
    marks = [i * step for i in range(int(round(arc / step)) + 1)]
    hold_n = max(1, int(round(hold_s * rate)))
    move_n = max(1, int(round(move_s * rate)))
    for i, mark in enumerate(marks):
        for _ in range(hold_n):
            yield mark
        if i + 1 < len(marks):
            nxt = marks[i + 1]
            for k in range(1, move_n + 1):
                # Smoothstep, so the ring eases out of one mark and into the next
                # instead of jumping.
                t = k / move_n
                yield mark + (nxt - mark) * t * t * (3 - 2 * t)


class Ring:
    """A ring on elastic bands that people grab, turn, hold and let go.

    Between grabs the bands and friction act as a damped spring pulling the ring
    back to centre, so it swings through centre a few times before settling.
    Angles here are signed about centre, so -135 to +135.
    """

    LIMIT = ARC_DEGREES / 2.0
    RESTITUTION = 0.3  # energy kept when it hits a stop

    def __init__(self, period, damping, rng):
        self.omega = 2 * math.pi / period
        self.zeta = damping
        self.rng = rng
        self.angle = 0.0
        self.vel = 0.0
        self.state = "idle"
        self.timer = rng.uniform(0.5, 3.0)
        self.target = 0.0
        self.start = 0.0
        self.elapsed = 0.0

    def _grab(self):
        """Someone takes hold and turns it somewhere new."""
        self.state = "turn"
        self.start = self.angle
        # People favour the big swings; the stops are the fun part.
        reach = self.rng.choice([self.rng.uniform(30, 90), self.rng.uniform(90, self.LIMIT)])
        self.target = reach if self.rng.random() < 0.5 else -reach
        # Human turning speed, roughly 50 to 130 degrees per second.
        speed = self.rng.uniform(50, 130)
        self.timer = max(0.25, abs(self.target - self.start) / speed)
        self.elapsed = 0.0

    def step(self, dt):
        self.timer -= dt
        if self.state == "turn":
            # A hand on the ring overrides the bands entirely.
            self.elapsed += dt
            total = self.elapsed + max(self.timer, 0.0)
            t = 1.0 if total <= 0 else min(1.0, self.elapsed / total)
            smooth = t * t * (3 - 2 * t)
            prev = self.angle
            self.angle = self.start + (self.target - self.start) * smooth
            self.vel = (self.angle - prev) / dt if dt > 0 else 0.0
            if self.timer <= 0:
                self.state = "hold"
                self.timer = self.rng.uniform(0.3, 2.5)
        elif self.state == "hold":
            # Held against the bands, with a little hand tremor.
            self.vel = 0.0
            self.angle += self.rng.gauss(0, 0.05)
            if self.timer <= 0:
                self.state = "idle"
                self.timer = self.rng.uniform(1.0, 8.0)
        else:
            # Let go: damped spring back towards centre.
            accel = -self.omega ** 2 * self.angle - 2 * self.zeta * self.omega * self.vel
            self.vel += accel * dt
            self.angle += self.vel * dt
            if self.timer <= 0 and abs(self.vel) < 40:
                self._grab()

        if abs(self.angle) > self.LIMIT:
            self.angle = math.copysign(self.LIMIT, self.angle)
            self.vel = -self.vel * self.RESTITUTION
        return self.angle


# --- Hold detection check -------------------------------------------------

def check_holds(samples, expected, min_hold_ms=1000, noise_multiple=4.0):
    """Mirror of OSCPlay's HoldDetector, to confirm a sweep will calibrate.

    Returns (number of holds found, stillness threshold used).
    """
    if len(samples) < 2:
        return 0, 0.0
    # estimateNoise: lower quartile of the per-axis consecutive differences.
    variance = 0.0
    for k in range(3):
        diffs = sorted(abs(samples[i][1][k] - samples[i - 1][1][k]) for i in range(1, len(samples)))
        sigma = diffs[len(diffs) // 4] / (0.3186 * math.sqrt(2))
        variance += sigma * sigma
    threshold = math.sqrt(variance) * noise_multiple

    def dist(a, b):
        return math.sqrt(sum((p - q) ** 2 for p, q in zip(a, b)))

    holds = 0
    n = len(samples)
    start = 0
    while start < n:
        total = list(samples[start][1])
        end = start + 1
        while end < n:
            mean = [c / (end - start) for c in total]
            nxt = samples[end][1]
            if dist(mean, nxt) > threshold:
                break
            for k in range(3):
                total[k] += nxt[k]
            end += 1
        if samples[end - 1][0] - samples[start][0] >= min_hold_ms:
            holds += 1
            start = end
        else:
            start += 1
    return holds, threshold


# --- Main -----------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--host", default="127.0.0.1",
                        help="Destination IP, e.g. the OSCPlay machine (default: %(default)s)")
    parser.add_argument("--port", type=int, default=8000,
                        help="Destination port; OSCPlay's default input (default: %(default)s)")
    parser.add_argument("--address", default=None,
                        help="OSC address (default: /mag<N>/xyz, matching the sketch)")
    parser.add_argument("--mag", type=int, default=1, choices=sorted(PROFILES),
                        help="Which tower to simulate; picks its sensor distortion (default: %(default)s)")
    parser.add_argument("--mode", choices=["play", "calibrate"], default="play",
                        help="People turning the ring, or the calibration sweep (default: %(default)s)")
    parser.add_argument("--rate", type=float, default=80,
                        help="Messages per second; the sketch sends 80 (default: %(default)s)")
    parser.add_argument("--duration", type=float, default=0,
                        help="Seconds to run, 0 = until Ctrl-C (play mode only)")
    parser.add_argument("--noise", type=float, default=DEFAULT_NOISE,
                        help="Per-axis sensor noise in counts (default: %(default)s)")
    parser.add_argument("--seed", type=int, default=None, help="Random seed, for repeatable runs")
    parser.add_argument("--csv", default=None,
                        help="Also write the samples as timestamp,magx,magy,magz")
    parser.add_argument("--no-send", action="store_true",
                        help="Don't send OSC; useful with --csv or --check")
    cal = parser.add_argument_group("calibrate mode")
    cal.add_argument("--hold", type=float, default=3.0,
                     help="Seconds held at each mark (default: %(default)s)")
    cal.add_argument("--move", type=float, default=1.0,
                     help="Seconds to move between marks (default: %(default)s)")
    cal.add_argument("--arc", type=float, default=ARC_DEGREES,
                     help="Total travel in degrees (default: %(default)s)")
    cal.add_argument("--step", type=float, default=MARK_SPACING_DEGREES,
                     help="Mark spacing in degrees (default: %(default)s)")
    cal.add_argument("--check", action="store_true",
                     help="Report how many holds OSCPlay would find in the sweep")
    play = parser.add_argument_group("play mode")
    play.add_argument("--period", type=float, default=2.8,
                      help="Natural period of the ring on its bands, in seconds (default: %(default)s)")
    play.add_argument("--damping", type=float, default=0.18,
                      help="Damping ratio; below 1 swings through centre (default: %(default)s)")
    args = parser.parse_args()

    if args.rate <= 0:
        parser.error("--rate must be positive")
    address = args.address if args.address else "/mag%d/xyz" % args.mag

    rng = random.Random(args.seed)
    sensor = Sensor(PROFILES[args.mag], args.noise, rng)
    sock = None if args.no_send else socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    dest = (args.host, args.port)
    period = 1.0 / args.rate

    if args.mode == "calibrate":
        marks = int(round(args.arc / args.step)) + 1
        total = marks * args.hold + (marks - 1) * args.move
        angles = calibration_angles(args.rate, args.hold, args.move, args.arc, args.step)
        print("Calibration sweep for tower %d: %d marks %g deg apart, "
              "%gs hold, %gs move, %.0fs total."
              % (args.mag, marks, args.step, args.hold, args.move, total), flush=True)
    else:
        ring = Ring(args.period, args.damping, rng)
        angles = None

    where = "nowhere (--no-send)" if args.no_send else "%s:%d" % dest
    print("Sending %s to %s at %g msg/s. Ctrl-C to stop." % (address, where, args.rate), flush=True)

    samples = []
    start = time.monotonic()
    next_send = start
    count = 0
    try:
        while True:
            # Both modes run on the sample clock rather than the wall clock. When
            # sending, the loop is paced to the wall clock so the two agree; with
            # --no-send it just fast-forwards, and --seed stays reproducible.
            t = count * period
            if args.mode == "calibrate":
                mark = next(angles, None)
                if mark is None:
                    break
            else:
                if args.duration and t >= args.duration:
                    break
                # The ring's own angle is signed about centre; the sensor and the
                # calibration marks both count up from the 0 degree stop.
                mark = ring.step(period) + Ring.LIMIT

            values = sensor.read(mark)
            if sock is not None:
                sock.sendto(osc_message(address, values), dest)
            if args.csv or args.check:
                samples.append((count * 1000.0 / args.rate, values))
            count += 1

            if count % max(1, int(args.rate)) == 0:
                print("t=%6.2fs  %6.1f deg   %8.0f %8.0f %8.0f"
                      % (t, mark, values[0], values[1], values[2]), flush=True)

            next_send += period
            if sock is not None:
                # Schedule against the start time so the rate doesn't drift.
                time.sleep(max(0.0, next_send - time.monotonic()))
    except KeyboardInterrupt:
        print()

    print("Sent %d messages" % count)

    if args.csv:
        with open(args.csv, "w", newline="") as f:
            w = csv.writer(f)
            w.writerow(["timestamp", "magx", "magy", "magz"])
            for ts, v in samples:
                w.writerow([int(ts)] + [round(c) for c in v])
        print("Wrote %s (%d rows)" % (args.csv, len(samples)))

    if args.check:
        if args.mode != "calibrate":
            print("--check only means anything for --mode calibrate", file=sys.stderr)
            return 1
        expected = int(round(args.arc / args.step)) + 1
        holds, threshold = check_holds(samples, expected)
        print("Hold detection: found %d holds for %d marks (stillness threshold %.0f counts)"
              % (holds, expected, threshold))
        if holds != expected:
            print("The sweep would NOT calibrate cleanly. Try a longer --hold or a shorter --move.",
                  file=sys.stderr)
            return 1
        print("The sweep would calibrate cleanly.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
