# Interlace Sensors

Each of the three Interlace towers reports which way it is facing. This guide covers
calibrating those sensors and simulating them when the installation isn't in front of you.

## What the sensor measures

A tower is a ring about 6 feet across that people turn. Elastic bands pull it back to a centre
rest position, and welded stops limit it to 135 degrees either side of centre, so **270 degrees
of travel** in total.

Inside is a magnetometer. As the ring turns, the Earth's magnetic field sweeps around in the
sensor's frame, so `atan2(y, x)` gives an absolute angle. The sensor is a compass being used as
an absolute rotary encoder, which is why it needs no index pulse or homing move.

It does need calibrating. The steel in the installation bends the field, so the readings don't
trace a neat circle centred on the origin:

- **Hard iron** shifts the circle away from the origin.
- **Soft iron** squashes it into an ellipse and rotates that ellipse.
- The sensor board is mounted at a tilt, so turning the ring also swings the Z reading.

Calibration measures that distortion once per install and corrects for it. Every tower sits in
different steel, so each needs its own.

## Hardware

- **WT32-ETH01** ESP32 board with built-in wired Ethernet, one per tower.
- **Adafruit LIS3MDL** 3-axis magnetometer over I2C, run in continuous mode at 80 Hz, +/-4 gauss.

Readings are raw 16-bit counts, roughly +/-2000 around a hard-iron offset of a few thousand.

## The two sketches

Both live here and are Arduino IDE sketches. (`sensor-firmware/` at the repo root is a separate
PlatformIO project for the same hardware, with its own README.)

| | `Interlace_Arduino/` | `Interlace_Arduino_Raw/` |
|---|---|---|
| Sends | one float, the finished angle | three floats, raw x/y/z counts |
| Address | `/lx/modulation/Angles/angle3` | `/mag<N>/xyz` |
| Rate | 60 Hz | 80 Hz |
| Calibration | on the ESP32, stored in flash | none; done downstream in OSCPlay |
| Filtering | IIR low-pass, ~80 ms | none |
| Receives OSC | yes, for calibration commands | no, publish only |

**Use the raw sketch for the current setup.** It streams uncorrected readings and lets OSCPlay
own the calibration, which means recalibrating never involves reflashing. The full sketch is the
older self-contained path, kept because it still works; see
[Calibrating on the device](#calibrating-on-the-device-legacy) below.

Both publish to **192.168.1.110:8000**, which is OSCPlay's input.

### Per-unit network settings

Edit the config block at the top of the sketch before flashing each board:

```c
String target = "/mag1/xyz";              // /mag2/xyz, /mag3/xyz
const char* hostname = "interlace1";
const IPAddress ip(192, 168, 1, 105);     // must differ per unit
```

Both sketches currently ship with `192.168.1.105`. **Give each board its own address** or two
units on the wire will fight over it. The destination (`192.168.1.110:8000`) is the same for all
three.

`target` matters as much as the IP, and it must carry this board's tower number. OSCPlay's
`Interlace Magnometer` node listens on exactly this address, and a node only sees messages whose
address matches its pattern, so a board publishing the wrong number is handled as the wrong tower.

## Signal path

```
ESP32  --raw xyz-->  OSCPlay 192.168.1.110:8000  --node chain-->  Chromatik 127.0.0.1:3030
```

OSCPlay's chain for the `default` output:

| # | Node | Does |
|---|---|---|
| 0 | Interlace Magnometer `["1"]` | listens on `/mag1/xyz`; applies the tower's calibration; 3 raw counts in, **degrees** out (0-270) |
| 1 | Rename | `/mag(\d)/xyz` -> `/lx/modulation/Angles/angle$1` |
| 2 | Remap Float | `0-270` -> `0-1`, clamped |

Node 2 is not optional. Chromatik's `angle1`-`angle3` are normalized parameters, and LX applies
incoming OSC with `setNormalized()`, which expects 0-1 and clamps anything larger. Without the
remap, degrees arrive and the towers sit pinned at full deflection.

The Rename and Remap Float nodes already handle all three towers. Adding tower 2 and 3 means
adding one `Interlace Magnometer` node each, with args `["2"]` and `["3"]`.

## Calibrating a tower

Calibration is a recording of the tower visiting known positions. You turn it to each of 28 marks
10 degrees apart, from the 0 degree stop to the 270 degree stop, holding still for 3 seconds at
each one. OSCPlay records the readings, finds the still stretches, and pairs each with its mark.

The full procedure for the real installation, including the marking-out and the two-person
workflow, is in OSCPlay's own guide:

> `OSCPlay/docs/interlace_calibration_guide.md`

The short version, per tower:

1. In OSCPlay click **Start Recording**, name it for the tower and date, e.g. `tower2-2026-09-23`, and set **Address filter** to that tower's sensor, e.g. `/mag2/xyz`.
2. Turn the tower to the 0 degree stop. **Hold still 3 seconds.**
3. Move to each 10 degree mark in order, holding still 3 seconds at each. Keep moving in between
   and don't revisit a mark.
4. Finish against the 270 degree stop, hold 3 seconds, **Stop Recording**.
5. **Tools > Sensor Calibration...**, set **Preset** to this tower (`Interlace Mag 2`), pick the
   recording, and wait for the green message ending **"All 28 marks found."**
6. **Save.** The tower uses it immediately; no restart needed.

Only the green message lets you save. Yellow means the holds weren't found cleanly:

- **Fewer than 28** — some stops were too short. Record again, counting to 3 at every mark.
- **More than 28** — the tower paused between marks. Keep it moving between stops.
- **"No messages on /mag2/xyz..."** — wrong preset or wrong recording, or that
  tower's sensor isn't sending. Check with **Monitor**.

### Filter the recording to one tower

All three sensor boards start streaming the moment they are powered and on the network, so a
recording with no filter captures all three. The marks are still found from the right tower, so
the calibration works either way, but the file is three times the size and harder to inspect.

The filter has to match the address the boards **actually publish**, which is the sketch's
`target`. Recording captures the raw input before any node runs, so a Rename node later in the
chain cannot help here. Since OSCPlay's magnetometer node and its calibration presets both work on
`/mag<N>/xyz`, the same address the firmware sends, the filter is simply that tower's address.

The **Address filter** box in the recording dialog takes a regex that must match the whole
address, the same rule node address patterns use:

| Filter | Records |
|---|---|
| *(blank)* | everything |
| `/mag2/xyz` | tower 2 only |
| `/mag[23]/xyz` | towers 2 and 3 |
| `/mag./xyz` | all three, nothing else |

The box remembers what you last typed, so when working through all three towers in one session,
remember to change the number each time.

### From the command line

`calibrate.sh` builds a calibration from an existing recording without the GUI:

```bash
cd ../../OSCPlay
./calibrate.sh --project interlacetest1 --recording tower2-2026-09-23 --preset interlace --mag 2
```

Add `--dry-run` to see what it found without saving. `--csv <file>` reads
`timestamp,magx,magy,magz` rows instead of a recording, and `--min-hold` / `--threshold` override
the hold detection if a recording is marginal.

## Running simulations

`simulate_mag.py` stands in for a tower's ESP32. It sends the same message the raw sketch sends,
three floats of magnetometer counts, so OSCPlay can't tell the difference. No dependencies beyond
Python 3.

Its sensor model was fitted to a real recording, so the output has realistic hard iron, soft iron,
mount tilt and noise. Each `--mag` has its own distortion, as the real towers do.

### Simulating people using the installation

```bash
python3 simulate_mag.py --mag 1
```

Runs until Ctrl-C. Someone grabs the ring, turns it at a human speed, holds it a moment and lets
go; the bands swing it back through centre a few times before it settles; then a pause before the
next person. It hits the stops at +/-135 degrees and uses both directions.

### Simulating a calibration sweep

```bash
python3 simulate_mag.py --mode calibrate --mag 2
python3 simulate_mag.py --mode calibrate --mag 3
```

Each runs **111 seconds** (28 marks x 3s hold, plus 27 x 1s moves) and exits on its own. Record it
in OSCPlay exactly as you would a real tower, then run **Tools > Sensor Calibration...** on it.
This is the way to exercise the whole calibration pipeline without touching the installation.

To check a sweep will calibrate cleanly before spending two minutes recording it:

```bash
python3 simulate_mag.py --mode calibrate --mag 2 --check --no-send
# Hold detection: found 28 holds for 28 marks (stillness threshold 191 counts)
# The sweep would calibrate cleanly.
```

`--check` runs the same hold detection OSCPlay uses, so a pass here means a green message there.
It's also how you tell whether `--hold`, `--move` or `--noise` changes are still usable.

### Addresses line up by default

`--mag N` sets the address to `/magN/xyz`, the same thing the sketch publishes and the same thing
OSCPlay's magnetometer node and calibration presets expect, so `--address` is only needed to
simulate something unusual. Use the same address as the recording's **Address filter**.

### Options

| Option | Default | |
|---|---|---|
| `--host` | `127.0.0.1` | destination IP; use `192.168.1.110` from another machine |
| `--port` | `8000` | OSCPlay's input port |
| `--address` | `/mag<N>/xyz` | matches the sketch; rarely needs changing |
| `--mag 1\|2\|3` | `1` | which tower, and so which sensor distortion |
| `--mode play\|calibrate` | `play` | |
| `--rate` | `80` | messages per second, as the sketch sends |
| `--duration` | `0` | seconds, 0 = until Ctrl-C (play mode) |
| `--noise` | `28` | per-axis noise in counts |
| `--seed` | | fixes the random sequence, for repeatable runs |
| `--csv FILE` | | also write `timestamp,magx,magy,magz` |
| `--no-send` | | don't send; fast-forwards for `--csv` or `--check` |

Calibrate mode: `--hold` (3.0s), `--move` (1.0s), `--arc` (270), `--step` (10), `--check`.

Play mode: `--period` (2.8s, the ring's natural period on its bands), `--damping` (0.18; below 1
swings through centre).

`--seed` makes a run byte-identical, which is what you want when comparing two calibrations or
filing a bug.

## Calibrating on the device (legacy)

The full `Interlace_Arduino/` sketch does the correction itself and stores five values in ESP32
flash (NVS namespace `calvals`): `xoffset`, `yoffset`, `xyscale`, `angmin`, `angmax`. The scripts
in `Interlace/` drive it from a Raspberry Pi:

```bash
cd Interlace
python3 osc.py        # edit ip_address at the top first
```

`osc.py` asks the board for 2000 raw samples, saves them to `calibration.npy`, fits an ellipse
(`processdat.py`) to recover the offsets and axis ratio, finds the mechanical travel limits from
the occupied angles, and writes all five back with `/writecals`. The board then publishes a
finished 0-1 angle.

Other scripts there: `osc_calibrate_RW.py` reads and writes the five values by hand,
`circtest.py` unit-tests the travel-limit detection, and `test.py` / `sensortestview.py` draw a
live XY scope. Note the two viewers expect the older three-address format
(`/lx/modulation/Mag1/magx`, `magy`, `magz`) on port 3030, not the current single three-argument
message, so they need adapting before they'll show anything from the raw sketch.

This path is harder to maintain than the OSCPlay one: recalibrating means running the Pi scripts
against each board, and the correction is baked into whatever is flashed. Prefer the OSCPlay path
for new work.

## Reference

### Ports

| Port | |
|---|---|
| 8000 | OSCPlay input, where the sketches send (UDP) |
| 3030 | OSCPlay output to Chromatik |
| 7770 | OSCPlay MCP server, `http://127.0.0.1:7770/mcp` |
| 7321 | full sketch: receives calibration commands |
| 7555 | full sketch: sends replies |

### OSC addresses

| Address | |
|---|---|
| `/mag<N>/xyz` | the raw sketch's output, 3 floats, and what the calibration and `Interlace Magnometer` node read |
| `/lx/modulation/Angles/angle<N>` | what Chromatik expects, normalized 0-1 |

In Chromatik, `AngleModulator` turns that 0-1 into real rotation as `-130 + 260 * value` degrees
and moves the model's LED positions to match.

### Files

| | |
|---|---|
| `simulate_mag.py` | tower simulator, both modes |
| `Interlace_Arduino_Raw/` | raw streaming firmware (current) |
| `Interlace_Arduino/` | self-calibrating firmware (legacy) |
| `Interlace/` | Raspberry Pi scripts for the legacy on-device calibration |
