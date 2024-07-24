import argparse
import math
import time
from pythonosc import udp_client
from pythonosc import osc_message_builder

def generate_sine_value(period, elapsed_time):
    return (math.sin(2 * math.pi * elapsed_time / period) + 1) / 2

def send_osc_message(client, address, value):
    msg = osc_message_builder.OscMessageBuilder(address=address)
    msg.add_arg(value)
    client.send(msg.build())

def main(period1, period2, period3, requests_per_second):
    client = udp_client.SimpleUDPClient("127.0.0.1", 3030)

    start_time = time.time()
    while True:
        current_time = time.time()
        elapsed_time = current_time - start_time

        value1 = generate_sine_value(period1, elapsed_time)
        value2 = generate_sine_value(period2, elapsed_time)
        value3 = generate_sine_value(period3, elapsed_time)

        send_osc_message(client, "/lx/modulation/Angles/angle1", value1)
        send_osc_message(client, "/lx/modulation/Angles/angle2", value2)
        send_osc_message(client, "/lx/modulation/Angles/angle3", value3)

        time.sleep(1 / requests_per_second)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate and send sine wave values via OSC.")
    parser.add_argument("p1", type=float, help="Period for the first sine wave")
    parser.add_argument("p2", type=float, help="Period for the second sine wave")
    parser.add_argument("p3", type=float, help="Period for the third sine wave")
    parser.add_argument("fps", type=float, help="Number of requests per second")

    args = parser.parse_args()

    main(args.p1, args.p2, args.p3, args.fps)
