import json
from collections import defaultdict
import argparse
import sys

def process_mag_data(input_filename, output_filename):
    # Dictionary to store magnetic field components by received_offset
    mag_readings = defaultdict(dict)

    # Read and process input file
    with open(input_filename, 'r') as f:
        for line in f:
            # Parse JSON data
            data = json.loads(line)
            offset = data['received_offset']
            msg = data['msg']

            # Skip non-magnetic field entries
            if 'Angles' in msg:
                continue

            # Extract component (magx, magy, or magz) and value
            parts = msg.split()
            component = parts[0].split('/')[-1]  # Get last part of path
            value = float(parts[1])

            # Store the component value
            mag_readings[offset][component] = value

    # Write combined readings to output file
    with open(output_filename, 'w') as f:
        for offset in sorted(mag_readings.keys()):
            # Only write complete entries (having all three components)
            if all(comp in mag_readings[offset] for comp in ['magx', 'magy', 'magz']):
                x = mag_readings[offset]['magx']
                y = mag_readings[offset]['magy']
                z = mag_readings[offset]['magz']
                # Create output in the same JSON format
                output = {
                    "received_offset": offset,
                    "msg": f"/lx/modulation/Mag1/mag {x} {y} {z}"
                }
                json.dump(output, f)
                f.write('\n')

def main():
    # Set up argument parser
    parser = argparse.ArgumentParser(description='Process magnetic field data and combine x,y,z components.')
    parser.add_argument('input_file', help='Input file containing the magnetic field readings')
    parser.add_argument('output_file', help='Output file for the processed data')

    # Parse arguments
    args = parser.parse_args()

    try:
        process_mag_data(args.input_file, args.output_file)
    except FileNotFoundError:
        print(f"Error: Could not find input file '{args.input_file}'", file=sys.stderr)
        sys.exit(1)
    except PermissionError:
        print(f"Error: Permission denied when accessing files", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError:
        print("Error: Invalid JSON data in input file", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Error: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
