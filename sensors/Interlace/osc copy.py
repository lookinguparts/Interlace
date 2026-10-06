from pythonosc import osc_message_builder
from pythonosc import udp_client
from pythonosc import dispatcher
from pythonosc import osc_server
import numpy as np
import socket


dat = np.empty((0, 2))
# Send OSC packet
#ip_address = socket.gethostbyname("interlaced2")
ip_address="192.168.1.103"
print("IP address of 'interlace1':", ip_address)
#client = udp_client.SimpleUDPClient("192.168.68.115", 7321)
client = udp_client.SimpleUDPClient(ip_address, 7321)

# Receive OSC response
def receive_handler(address, *args):
    global dat
    
    print(f"Received OSC message: {address} {args}")
    if args[2]=='done':
        print('recieved all calibration data')
        np.save('calibration.npy',dat)
        server.shutdown()
        
    if args[2]=='hello':
        dat = np.append(dat, [[args[0], args[1]]], axis=0)
        print(dat)

dispatcher = dispatcher.Dispatcher()
dispatcher.map("/reply", receive_handler)
client.send_message("/calibrate", "get cal data")
server = osc_server.ThreadingOSCUDPServer(("0.0.0.0", 7555), dispatcher)
print("Listening for OSC messages on port 5000...")
server.serve_forever()

print('done')