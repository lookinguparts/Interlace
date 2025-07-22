import argparse
import json
import socket
import sys

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
    parser = argparse.ArgumentParser(description="Run an OSCQuery against a target host and port.")
    parser.add_argument("host", help="Target host")
    parser.add_argument("port", type=int, help="Target port")

    args = parser.parse_args()

    run_oscquery(args.host, args.port)

if __name__ == "__main__":
    main()
