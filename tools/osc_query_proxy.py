import http.server
import socketserver
import json
import urllib.parse
from zeroconf import ServiceBrowser, Zeroconf
import socket
import threading
import time

# Global variable to store discovered services
discovered_services = {}

class OSCQueryListener:
    def remove_service(self, zeroconf, type, name):
        print(f"Service {name} removed")
        discovered_services.pop(name, None)

    def add_service(self, zeroconf, type, name):
        info = zeroconf.get_service_info(type, name)
        if info:
            discovered_services[name] = info
            print(f"Service {name} added, service info: {info}")

    def update_service(self, zeroconf, type, name):
        info = zeroconf.get_service_info(type, name)
        if info:
            discovered_services[name] = info
            print(f"Service {name} updated, service info: {info}")

def start_discovery():
    zeroconf = Zeroconf()
    listener = OSCQueryListener()
    browser = ServiceBrowser(zeroconf, "_oscjson._tcp.local.", listener)
    return zeroconf, browser

def stop_discovery(zeroconf, browser):
    browser.cancel()
    zeroconf.close()

def discover_oscquery_service():
    if not discovered_services:
        return None

    # For simplicity, we'll use the first service found
    service_name, service_info = next(iter(discovered_services.items()))
    host = socket.inet_ntoa(service_info.addresses[0])
    port = service_info.port
    return host, port

def query_osc_service(host, port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        sock.connect((host, port))
        sock.sendall(b"GET / HTTP/1.1\r\nHost: " + host.encode() + b"\r\n\r\n")
        response = b""
        while True:
            chunk = sock.recv(4096)
            if not chunk:
                break
            response += chunk
        headers, _, body = response.partition(b"\r\n\r\n")
        return json.loads(body)
    except Exception as e:
        return {"error": str(e)}
    finally:
        sock.close()

class OSCQueryHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        parsed_path = urllib.parse.urlparse(self.path)
        if parsed_path.path == '/discover':
            self.handle_discover()
        elif parsed_path.path == '/query':
            self.handle_query()
        else:
            super().do_GET()

    def handle_discover(self):
        result = discover_oscquery_service()
        if result:
            host, port = result
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({"host": host, "port": port}).encode())
        else:
            self.send_error(404, "OSCQuery service not found")

    def handle_query(self):
        params = urllib.parse.parse_qs(urllib.parse.urlparse(self.path).query)
        host = params.get('host', [''])[0]
        port = params.get('port', [''])[0]

        if not host or not port:
            self.send_error(400, "Missing host or port parameter")
            return

        result = query_osc_service(host, int(port))
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps(result).encode())

def run_server(port=8000):
    with socketserver.TCPServer(("", port), OSCQueryHandler) as httpd:
        print(f"Serving at port {port}")
        httpd.serve_forever()

if __name__ == "__main__":
    zeroconf, browser = start_discovery()
    server_thread = threading.Thread(target=run_server)
    server_thread.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("Shutting down...")
    finally:
        stop_discovery(zeroconf, browser)
        # Note: This won't actually stop the server thread,
        # you'll need to forcibly terminate the script