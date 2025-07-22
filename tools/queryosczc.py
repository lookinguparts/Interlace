import argparse
import json
import socket
import sys
import time
from zeroconf import ServiceBrowser, Zeroconf

class OSCQueryListener:
    def __init__(self):
        self.services = {}

    def remove_service(self, zeroconf, type, name):
        print(f"Service {name} removed")

    def add_service(self, zeroconf, type, name):
        info = zeroconf.get_service_info(type, name)
        if info:
            self.services[name] = info
            print(f"Service {name} added, service info: {info}")

def discover_oscquery_service():
    zeroconf = Zeroconf()
    listener = OSCQueryListener()
    browser = ServiceBrowser(zeroconf, "_oscjson._tcp.local.", listener)

    print("Searching for OSCQuery services...")
    time.sleep(3)  # Wait for 3 seconds to discover services

    zeroconf.close()

    if not listener.services:
        print("No OSCQuery services found.")
        return None

    # For simplicity, we'll use the first service found
    service_name, service_info = next(iter(listener.services.items()))
    host = socket.inet_ntoa(service_info.addresses[0])
    port = service_info.port

    print(f"Found OSCQuery service: {service_name}")
    print(f"Host: {host}, Port: {port}")

    return host, port

def run_oscquery(host, port):
    # Create a TCP socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    try:
        # Connect to the server
        sock.connect((host, port))

        # Send an empty GET request
        sock.sendall(b"GET / HTTP/1.1\r\nHost: " + host.encode() + b"\r\n\r\n")

        # Receive the response
        response = b""
        while True:
            chunk = sock.recv(4096)
            if not chunk:
                break
            response += chunk

        # Parse the response
        headers, _, body = response.partition(b"\r\n\r\n")

        # Try to parse the body as JSON
        try:
            json_data = json.loads(body)
            print(json.dumps(json_data, indent=2))
        except json.JSONDecodeError:
            print(body.decode('utf-8'))

    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
    finally:
        sock.close()

def main():
    parser = argparse.ArgumentParser(description="Discover and run an OSCQuery against a target host.")
    parser.add_argument("--host", help="Target host (optional, will use Zeroconf if not provided)")
    parser.add_argument("--port", type=int, help="Target port (optional, will use Zeroconf if not provided)")

    args = parser.parse_args()

    if args.host and args.port:
        host, port = args.host, args.port
    else:
        result = discover_oscquery_service()
        if result:
            host, port = result
        else:
            print("No OSCQuery service found. Exiting.")
            return

    #run_oscquery(host, port)

if __name__ == "__main__":
    main()