#!/usr/bin/env python3

import json
import numpy as np
from scipy.interpolate import splprep, splev
from scipy.spatial.distance import cdist
from collections import defaultdict
import argparse
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import csv

class MagnetometerProcessor:
    def __init__(self):
        self.spline_x = None
        self.spline_y = None
        self.spline_z = None
        self.t_values = None

    def load_data_from_file(self, filepath):
        """
        Load and parse magnetometer data from JSON file.

        Args:
            filepath: Path to the JSON data file

        Returns:
            timestamps: Array of timestamp values
            magx: Array of x-axis magnetometer readings
            magy: Array of y-axis magnetometer readings
            magz: Array of z-axis magnetometer readings
        """
        data_points = defaultdict(list)

        with open(filepath, 'r') as file:
            for line in file:
                try:
                    entry = json.loads(line.strip())
                    timestamp = entry['received_offset']
                    msg = entry['msg']

                    path, value = msg.split(' ')

                    if 'Mag1' in path:
                        value = int(value)
                        if 'magx' in path:
                            data_points['magx'].append((timestamp, value))
                        elif 'magy' in path:
                            data_points['magy'].append((timestamp, value))
                        elif 'magz' in path:
                            data_points['magz'].append((timestamp, value))
                except json.JSONDecodeError:
                    print(f"Skipping malformed JSON line: {line.strip()}")
                except ValueError:
                    print(f"Skipping malformed message line: {line.strip()}")

        def process_component(data):
            if not data:
                return np.array([]), np.array([])
            timestamps, values = zip(*sorted(data))
            return np.array(timestamps), np.array(values)

        tx, magx = process_component(data_points['magx'])
        ty, magy = process_component(data_points['magy'])
        tz, magz = process_component(data_points['magz'])

        if len(magx) == 0 or len(magy) == 0 or len(magz) == 0:
            raise ValueError("Missing magnetometer data for one or more axes")

        # Align data points to same timestamps
        all_timestamps = sorted(set(tx) & set(ty) & set(tz))

        aligned_data = []
        for t in all_timestamps:
            x_idx = np.where(tx == t)[0][0]
            y_idx = np.where(ty == t)[0][0]
            z_idx = np.where(tz == t)[0][0]
            aligned_data.append((t, magx[x_idx], magy[y_idx], magz[z_idx]))

        timestamps, magx, magy, magz = zip(*aligned_data)
        return np.array(timestamps), np.array(magx), np.array(magy), np.array(magz)

    def smooth_data(self, magx, magy, magz, window_size):
        """
        Smooth the magnetometer data using a centered moving average.

        Args:
            magx, magy, magz: Magnetometer data arrays
            window_size: Size of the smoothing window

        Returns:
            Smoothed magnetometer data arrays
        """
        def moving_average(data, window_size):
            # Ensure window size is odd
            window_size = max(3, window_size if window_size % 2 == 1 else window_size + 1)
            half_window = window_size // 2

            smoothed = np.zeros_like(data)
            for i in range(len(data)):
                start = max(0, i - half_window)
                end = min(len(data), i + half_window + 1)
                window = data[start:end]
                smoothed[i] = np.mean(window)

            return smoothed

        # Apply moving average to each axis
        smooth_x = moving_average(magx, window_size)
        smooth_y = moving_average(magy, window_size)
        smooth_z = moving_average(magz, window_size)

        return smooth_x, smooth_y, smooth_z

    def strip_rest_periods(self, magx, magy, magz, window_size=20, threshold=50):
        """
        Remove the rest periods from the beginning and end of the data.

        Args:
            magx, magy, magz: Magnetometer data arrays
            window_size: Size of the window for detecting movement
            threshold: Threshold for movement detection

        Returns:
            Trimmed magnetometer data arrays
        """
        def rolling_change(data):
            changes = []
            for i in range(len(data) - window_size):
                window = data[i:i + window_size]
                change = np.max(window) - np.min(window)
                changes.append(change)
            return np.array(changes)

        changes_x = rolling_change(magx)
        changes_y = rolling_change(magy)
        changes_z = rolling_change(magz)
        total_changes = changes_x + changes_y + changes_z

        start_idx = 0
        for i in range(len(total_changes)):
            if total_changes[i] > threshold:
                start_idx = i
                break

        end_idx = len(total_changes)
        for i in range(len(total_changes) - 1, 0, -1):
            if total_changes[i] > threshold:
                end_idx = i + window_size
                break

        buffer_size = window_size
        start_idx = max(0, start_idx - buffer_size)
        end_idx = min(len(magx), end_idx + buffer_size)

        return magx[start_idx:end_idx], magy[start_idx:end_idx], magz[start_idx:end_idx]

    def fit_calibration_curve(self, magx, magy, magz, smoothing=0.1):
        """
        Fit a parametric spline to the calibration data.

        Args:
            magx, magy, magz: Magnetometer data arrays
            smoothing: Smoothing factor for the spline fit
        """
        points = np.column_stack([magx, magy, magz])

        t = np.zeros(len(points))
        for i in range(1, len(points)):
            t[i] = t[i-1] + np.linalg.norm(points[i] - points[i-1])
        t = t / t[-1]

        tck, u = splprep([magx, magy, magz], u=t, s=smoothing, k=3)
        self.spline_x, self.spline_y, self.spline_z = tck
        self.t_values = u

        return self

    def get_spline_points(self, num_points=1000):
        """
        Get points along the fitted spline.

        Returns:
            x, y, z: Arrays of points along the spline
        """
        if self.spline_x is None:
            raise ValueError("Must fit calibration curve first")

        t_fine = np.linspace(0, 1, num_points)
        x, y, z = splev(t_fine, (self.spline_x, self.spline_y, self.spline_z))
        return x, y, z

    def angle_from_reading(self, magx, magy, magz, num_points=1000):
        """
        Convert a magnetometer reading to an angle.

        Returns:
            angle: Estimated angle in degrees (0-270)
        """
        if self.spline_x is None:
            raise ValueError("Must fit calibration curve first")

        t_fine = np.linspace(0, 1, num_points)
        points = np.column_stack(splev(t_fine, (self.spline_x, self.spline_y, self.spline_z)))

        query_point = np.array([[magx, magy, magz]])
        distances = cdist(query_point, points)
        closest_idx = np.argmin(distances)

        t = t_fine[closest_idx]
        angle = t * 270.0

        return angle

    def point_from_angle(self, angle, num_points=1000):
        """
        Get the magnetometer readings corresponding to a specific angle.

        Args:
            angle: Desired angle in degrees (0-270)
            num_points: Number of points to use for interpolation

        Returns:
            tuple: (x, y, z) coordinates corresponding to the angle

        Raises:
            ValueError: If angle is outside valid range or spline not fitted
        """
        if self.spline_x is None:
            raise ValueError("Must fit calibration curve first")

        if angle < 0 or angle > 270:
            raise ValueError("Angle must be between 0 and 270 degrees")

        # Convert angle to parameter value
        t = angle / 270.0

        # Get point on spline
        x, y, z = splev([t], (self.spline_x, self.spline_y, self.spline_z))
        return float(x), float(y), float(z)

def visualize_3d(magx, magy, magz, processor, sample_point=None):
    """
    Create a 3D visualization of the magnetometer data and fitted spline.
    """
    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')

    # Plot original data points
    ax.scatter(magx, magy, magz, c='blue', s=1, alpha=0.5, label='Original Data')

    if processor.spline_x is not None:
        # Plot the fitted spline
        spline_x, spline_y, spline_z = processor.get_spline_points()
        ax.plot(spline_x, spline_y, spline_z, 'r-', linewidth=2, label='Fitted Spline')

        # Add markers at 10-degree intervals
        angles = np.arange(0, 271, 10)  # 0 to 270 degrees, step 10
        degree_points = [processor.point_from_angle(angle) for angle in angles]
        degree_x, degree_y, degree_z = zip(*degree_points)

        # Plot degree markers
        ax.scatter(degree_x, degree_y, degree_z, c='purple', s=50,
                  label='10° Intervals', zorder=5)

        # Add degree labels for every 30 degrees
        for angle in np.arange(0, 271, 30):
            x, y, z = processor.point_from_angle(angle)
            ax.text(x, y, z, f'{angle}°', color='purple', fontsize=8)

    # Plot sample point if provided
    if sample_point is not None:
        x, y, z = sample_point
        angle = processor.angle_from_reading(x, y, z)

        ax.scatter([x], [y], [z], c='green', s=100, marker='*', label='Sample Point')
        ax.text(x, y, z, f'  {angle:.1f}°', color='green', fontsize=10)

    ax.set_xlabel('Magnetic Field X')
    ax.set_ylabel('Magnetic Field Y')
    ax.set_zlabel('Magnetic Field Z')
    ax.set_title('3D Magnetometer Data with Fitted Spline')
    ax.legend()

    plt.show()


def visualize_3d_old(magx, magy, magz, processor, sample_point=None):
    """
    Create a 3D visualization of the magnetometer data and fitted spline.
    """
    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')

    ax.scatter(magx, magy, magz, c='blue', s=1, alpha=0.5, label='Original Data')

    if processor.spline_x is not None:
        spline_x, spline_y, spline_z = processor.get_spline_points()
        ax.plot(spline_x, spline_y, spline_z, 'r-', linewidth=2, label='Fitted Spline')

    if sample_point is not None:
        x, y, z = sample_point
        angle = processor.angle_from_reading(x, y, z)

        ax.scatter([x], [y], [z], c='green', s=100, marker='*', label='Sample Point')
        ax.text(x, y, z, f'  {angle:.1f}°', color='green', fontsize=10)

    ax.set_xlabel('Magnetic Field X')
    ax.set_ylabel('Magnetic Field Y')
    ax.set_zlabel('Magnetic Field Z')
    ax.set_title('3D Magnetometer Data with Fitted Spline')
    ax.legend()

    plt.show()

def save_to_csv(filepath, timestamps, magx, magy, magz):
    """
    Save the processed magnetometer data to a CSV file.
    """
    with open(filepath, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(['timestamp', 'magx', 'magy', 'magz'])
        for t, x, y, z in zip(timestamps, magx, magy, magz):
            writer.writerow([t, x, y, z])

def parse_sample_point(sample_str):
    """
    Parse a sample point string in the format "x,y,z".

    Args:
        sample_str: String in format "x,y,z"

    Returns:
        Tuple of (x, y, z) as floats

    Raises:
        ValueError if format is invalid
    """
    try:
        x, y, z = map(float, sample_str.split(','))
        return (x, y, z)
    except ValueError:
        raise ValueError("Sample point must be in the format 'x,y,z' (e.g., '-4391,2358,-3803')")

def main():
    parser = argparse.ArgumentParser(description='Process magnetometer data and fit calibration curve')
    parser.add_argument('filename', help='Path to the input JSON data file')
    parser.add_argument('--threshold', type=float, default=50,
                      help='Threshold for detecting movement (default: 50)')
    parser.add_argument('--window-size', type=int, default=20,
                      help='Window size for movement detection (default: 20)')
    parser.add_argument('--smoothing', type=float, default=0.1,
                      help='Smoothing factor for spline fitting (default: 0.1)')
    parser.add_argument('--datasmooth', type=int, default=0,
                      help='Window size for smoothing data before spline fitting (default: 0, no smoothing)')
    parser.add_argument('--output', type=str,
                      help='Path to save processed data as CSV')
    parser.add_argument('--sample', type=str,
                      help='Sample point to analyze in format "x,y,z" (e.g., "-4391,2358,-3803")')

    args = parser.parse_args()

    processor = MagnetometerProcessor()
    try:
        print(f"Loading data from {args.filename}")
        timestamps, magx, magy, magz = processor.load_data_from_file(args.filename)
        print(f"Loaded {len(timestamps)} data points")

        print(f"Stripping rest periods (threshold={args.threshold}, window_size={args.window_size})")
        magx_stripped, magy_stripped, magz_stripped = processor.strip_rest_periods(
            magx, magy, magz,
            window_size=args.window_size,
            threshold=args.threshold
        )
        print(f"Stripped data contains {len(magx_stripped)} points")

        # Apply data smoothing if requested
        if args.datasmooth > 0:
            print(f"Smoothing data (window_size={args.datasmooth})")
            magx_stripped, magy_stripped, magz_stripped = processor.smooth_data(
                magx_stripped, magy_stripped, magz_stripped,
                window_size=args.datasmooth
            )

        print(f"Fitting calibration curve (smoothing={args.smoothing})")
        processor.fit_calibration_curve(
            magx_stripped, magy_stripped, magz_stripped,
            smoothing=args.smoothing
        )

        if args.output:
            print(f"Saving processed data to {args.output}")
            start_idx = len(magx) - len(magx_stripped)
            stripped_timestamps = timestamps[start_idx:start_idx + len(magx_stripped)]
            save_to_csv(args.output, stripped_timestamps, magx_stripped, magy_stripped, magz_stripped)
            print("Data saved successfully")

        # Process sample point if provided
        sample_point = None
        if args.sample:
            try:
                sample_point = parse_sample_point(args.sample)
                angle = processor.angle_from_reading(*sample_point)
                print(f"\nSample point analysis:")
                print(f"Coordinates (x,y,z): {sample_point}")
                print(f"Computed angle: {angle:.1f}°")
            except ValueError as e:
                print(f"Error processing sample point: {e}")

        print("\nGenerating 3D visualization...")
        visualize_3d(magx_stripped, magy_stripped, magz_stripped, processor, sample_point)

    except Exception as e:
        print(f"Error processing file: {e}")
        return 1

    return 0

if __name__ == "__main__":
    main()