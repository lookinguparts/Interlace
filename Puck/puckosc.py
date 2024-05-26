import json
import sys
from pythonosc import udp_client

def send_osc_data(data):
    client = udp_client.SimpleUDPClient("localhost", 3030)

    # Values are -32K to +32K.  Normalize from 0 to 1 for Chromatik.
    max_acc = 32767
    acc_x = float(data["acc"]["x"])/max_acc
    acc_x = (acc_x + 1)/2
    acc_y = float(data["acc"]["y"])/max_acc
    acc_y = (acc_y + 1)/2
    acc_z = float(data["acc"]["z"])/max_acc
    acc_z = (acc_z + 1)/2

    max_gyro = 33000
    gyro_x = float(data["gyro"]["x"])/max_gyro
    gyro_x = (gyro_x + 1)/2
    gyro_y = float(data["gyro"]["y"])/max_gyro
    gyro_y = (gyro_y + 1)/2
    gyro_z = float(data["gyro"]["z"])/max_gyro
    gyro_z = (gyro_z + 1)/2

    # Send accelerometer data
    client.send_message("/lx/modulation/Puck-Accel/macro1", acc_x)
    client.send_message("/lx/modulation/Puck-Accel/macro2", acc_y)
    client.send_message("/lx/modulation/Puck-Accel/macro3", acc_z)

    client.send_message("/lx/modulation/Puck-Gyro/macro1", acc_x)
    client.send_message("/lx/modulation/Puck-Gyro/macro2", acc_x)
    client.send_message("/lx/modulation/Puck-Gyro/macro3", acc_x)


def main():
    for line in sys.stdin:
        if line.find("gyro") == -1:
            continue
        try:
            data = json.loads(line)
            send_osc_data(data)
        except json.JSONDecodeError:
            print("Invalid JSON format:", line)

if __name__ == "__main__":
    main()
