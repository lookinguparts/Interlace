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
        plot_magnetometer_data(mag_data, timestamps)
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