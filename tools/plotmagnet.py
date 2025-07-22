import json
import matplotlib.pyplot as plt
from collections import defaultdict
import sys
import argparse

def process_json_file(file_path):
    mag_data = defaultdict(lambda: defaultdict(list))
    timestamps = defaultdict(list)

    with open(file_path, 'r') as file:
        for line in file:
            data = json.loads(line)
            msg = data['msg'].split()
            timestamp = data['received_offset']

            mag_parts = msg[0].split('/')
            mag_num = mag_parts[3]  # e.g., 'Mag3'
            # If mag_num doesn't start with 'Mag', skip it.
            if not mag_num.startswith('Mag'):
                continue
            axis = mag_parts[4]  # e.g., 'magx'

            value = int(msg[1])

            mag_data[mag_num][axis].append(value)
            timestamps[mag_num].append(timestamp)

    return mag_data, timestamps

def print_data_info(mag_data, timestamps):
    print("Data points per magnetometer and axis:")
    for mag, data in mag_data.items():
        print(f"\n{mag}:")
        for axis, values in data.items():
            print(f"  {axis}: {len(values)} data points")
        print(f"  Timestamps: {len(timestamps[mag])} data points")

def plot_3d_scatter(mag_data):
    num_mags = len(mag_data)
    fig = plt.figure(figsize=(15, 5*num_mags))
    fig.suptitle('3D Magnetometer Data Visualization')

    for i, (mag, data) in enumerate(mag_data.items()):
        # Find the minimum length across all axes
        min_len = min(len(data[axis]) for axis in ['magx', 'magy', 'magz'])

        # Create 3D subplot
        ax = fig.add_subplot(num_mags, 1, i+1, projection='3d')

        # Get data for each axis
        x_data = data['magx'][:min_len]
        y_data = data['magy'][:min_len]
        z_data = data['magz'][:min_len]

        # Create scatter plot
        scatter = ax.scatter(x_data, y_data, z_data, c=range(min_len),
                           cmap='viridis', alpha=0.6)

        # Add colorbar to show time progression
        colorbar = plt.colorbar(scatter, ax=ax)
        colorbar.set_label('Time Progression')

        # Set labels and title
        ax.set_xlabel('X Magnetic Field')
        ax.set_ylabel('Y Magnetic Field')
        ax.set_zlabel('Z Magnetic Field')
        ax.set_title(f'{mag} - 3D Magnetic Field Distribution\n({min_len} data points)')

        # Add grid
        ax.grid(True)

    plt.tight_layout()
    plt.show()

def plot_magnetometer_data(mag_data, timestamps):
    num_mags = len(mag_data)
    fig, axs = plt.subplots(num_mags, 1, figsize=(12, 5*num_mags), squeeze=False)
    fig.suptitle('Magnetometer Data over Time')

    colors = {'magx': 'r', 'magy': 'g', 'magz': 'b'}

    for i, (mag, data) in enumerate(mag_data.items()):
        ax = axs[i, 0]
        for axis, values in data.items():
            # Use the minimum length to avoid dimension mismatch
            min_len = min(len(values), len(timestamps[mag]))
            ax.plot(timestamps[mag][:min_len], values[:min_len], color=colors[axis], label=f"{axis} ({len(values)} points)")

        ax.set_title(f'{mag} (Timestamps: {len(timestamps[mag])} points)')
        ax.set_ylabel('Value')
        ax.legend()
        ax.grid(True)

    axs[-1, 0].set_xlabel('Timestamp (Received Offset)')
    plt.tight_layout()
    plt.show()

def main():
    parser = argparse.ArgumentParser(description='Plot magnetometer data from a JSON file.')
    parser.add_argument('filename', type=str, help='Path to the JSON file containing magnetometer data')
    args = parser.parse_args()

    try:
        mag_data, timestamps = process_json_file(args.filename)
        print_data_info(mag_data, timestamps)
        plot_3d_scatter(mag_data)
    except FileNotFoundError:
        print(f"Error: The file '{args.filename}' was not found.")
        sys.exit(1)
    except json.JSONDecodeError:
        print(f"Error: The file '{args.filename}' is not a valid JSON file.")
        sys.exit(1)
    except Exception as e:
        print(f"An error occurred: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()