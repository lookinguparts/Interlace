import json
from collections import defaultdict

def process_json_file(file_path):
    # Initialize dictionaries to store min and max values for each magnetometer
    mag_data = defaultdict(lambda: defaultdict(lambda: {'min': float('inf'), 'max': float('-inf')}))

    with open(file_path, 'r') as file:
        for line in file:
            data = json.loads(line)
            msg = data['msg'].split()

            # Extract magnetometer number and axis
            mag_parts = msg[0].split('/')
            mag_num = mag_parts[3]  # e.g., 'Mag3'
            axis = mag_parts[4]  # e.g., 'magx'

            value = int(msg[1])

            # Update min and max values
            mag_data[mag_num][axis]['min'] = min(mag_data[mag_num][axis]['min'], value)
            mag_data[mag_num][axis]['max'] = max(mag_data[mag_num][axis]['max'], value)

    return mag_data

def print_results(mag_data):
    for mag, axes in mag_data.items():
        print(f"\n{mag}:")
        for axis, values in axes.items():
            print(f"  {axis}: min = {values['min']}, max = {values['max']}")

if __name__ == "__main__":
    file_path = "magnet2.json"  # Replace with your actual file path
    results = process_json_file(file_path)
    print_results(results)
