import argparse
import json
import time
from pythonosc import udp_client

def parse_osc_message(msg):
    parts = msg.split(' ', 1)
    address = parts[0]

    #args = json.loads(f"[{parts[1]}]") if len(parts) > 1 else []
    args = [float(x) for x in parts[1].split()] if len(parts) > 1 else []
    return address, args

def play_osc_file(filename, port, host='127.0.0.1', print_messages=False):
    client = udp_client.SimpleUDPClient(host, port)

    with open(filename, 'r') as file:
        messages = [json.loads(line) for line in file]

    start_time = time.time() * 1000
    for message in messages:
        current_time = time.time() * 1000
        time_to_wait = (start_time + message['received_offset'] - current_time) / 1000
        if time_to_wait > 0:
            time.sleep(time_to_wait)

        address, args = parse_osc_message(message['msg'])
        client.send_message(address, args)
        if print_messages:
            print(f"Sent: {message['msg']} (offset: {message['received_offset']}ms)")

def main():
    parser = argparse.ArgumentParser(description="OSC Playback Script")
    parser.add_argument("filename", help="The file containing saved OSC messages")
    parser.add_argument("port", type=int, help="The UDP port to send messages to")
    parser.add_argument("--host", default="127.0.0.1", help="The host to send messages to (default: 127.0.0.1)")
    parser.add_argument("--print", action="store_true", help="Print sent messages to console (default: False)")
    args = parser.parse_args()

    play_osc_file(args.filename, args.port, args.host, args.print)

if __name__ == "__main__":
    main()
