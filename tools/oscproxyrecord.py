import argparse
import asyncio
import signal
import json
import time
from pythonosc.osc_server import AsyncIOOSCUDPServer
from pythonosc.dispatcher import Dispatcher
from pythonosc.udp_client import SimpleUDPClient

class OSCLogger:
    def __init__(self, print_messages=False, forward_port=None, forward_host='127.0.0.1'):
        self.messages = []
        self.print_messages = print_messages
        self.forward_port = forward_port
        self.forward_host = forward_host
        self.forward_client = SimpleUDPClient(forward_host, forward_port) if forward_port else None
        self.start_time = None

    def osc_handler(self, address, *args):
        current_time = time.time() * 1000  # Current time in milliseconds
        if self.start_time is None:
            self.start_time = current_time

        original_message = f"{address} {' '.join(map(str, args))}"
        wrapped_message = {
            "received_offset": int(current_time - self.start_time),
            "msg": original_message
        }

        self.messages.append(wrapped_message)

        if self.print_messages:
            print(f"Received: {json.dumps(wrapped_message)}")

        if self.forward_client:
            self.forward_client.send_message(address, args)

    async def loop(self):
        print("Press Enter to save and exit...")
        await asyncio.get_event_loop().run_in_executor(None, input)

def main():
    parser = argparse.ArgumentParser(description="OSC Listener, Logger, and Forwarder")
    parser.add_argument("port", type=int, help="The UDP port to listen on")
    parser.add_argument("filename", help="The filename to save the OSC messages")
    parser.add_argument("--print", action="store_true", help="Print received messages to console")
    parser.add_argument("--portfwd", type=int, help="Port to forward messages to")
    parser.add_argument("--hostfwd", default="127.0.0.1", help="Host to forward messages to (default: 127.0.0.1)")
    args = parser.parse_args()

    osc_logger = OSCLogger(print_messages=args.print, forward_port=args.portfwd, forward_host=args.hostfwd)
    dispatcher = Dispatcher()
    dispatcher.set_default_handler(osc_logger.osc_handler)

    async def init_main():
        server = AsyncIOOSCUDPServer(("0.0.0.0", args.port), dispatcher, asyncio.get_event_loop())
        transport, protocol = await server.create_serve_endpoint()

        await osc_logger.loop()

        transport.close()
        with open(args.filename, 'w') as f:
            for message in osc_logger.messages:
                f.write(f"{json.dumps(message)}\n")
        print(f"Messages saved to {args.filename}")

    asyncio.run(init_main())

if __name__ == "__main__":
    main()
