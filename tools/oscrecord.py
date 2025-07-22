import argparse
import asyncio
import signal
from pythonosc.osc_server import AsyncIOOSCUDPServer
from pythonosc.dispatcher import Dispatcher

"pip install python-osc"

class OSCLogger:
    def __init__(self):
        self.messages = []

    def osc_handler(self, address, *args):
        message = f"{address} {' '.join(map(str, args))}"
        self.messages.append(message)
        print(f"Received: {message}")

    async def loop(self):
        print("Press Enter to save and exit...")
        await asyncio.get_event_loop().run_in_executor(None, input)

def main():
    parser = argparse.ArgumentParser(description="OSC Listener and Logger")
    parser.add_argument("port", type=int, help="The UDP port to listen on")
    parser.add_argument("filename", help="The filename to save the OSC messages")
    args = parser.parse_args()

    osc_logger = OSCLogger()
    dispatcher = Dispatcher()
    dispatcher.set_default_handler(osc_logger.osc_handler)

    async def init_main():
        server = AsyncIOOSCUDPServer(("0.0.0.0", args.port), dispatcher, asyncio.get_event_loop())
        transport, protocol = await server.create_serve_endpoint()

        await osc_logger.loop()

        transport.close()
        with open(args.filename, 'w') as f:
            for message in osc_logger.messages:
                f.write(f"{message}\n")
        print(f"Messages saved to {args.filename}")

    asyncio.run(init_main())

if __name__ == "__main__":
    main()

