from pythonosc import osc_message_builder
from pythonosc import udp_client
from pythonosc import dispatcher
from pythonosc import osc_server
import numpy as np
import cv2
import socket
from matplotlib import pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import serial.tools.list_ports
import math
dat = np.empty((0, 2))
image = np.zeros((500, 500, 3), dtype=np.uint8)
# Send OSC packet
#ip_address = socket.gethostbyname("interlaced2")
ip_address="192.168.1.103"
print("IP address of 'interlace1':", ip_address)
#client = udp_client.SimpleUDPClient("192.168.68.115", 7321)
client = udp_client.SimpleUDPClient(ip_address, 7321)

def receive_handler(address, *args):
    print(f"Received OSC message: {address} {args}")
    server.shutdown()



dispatcher = dispatcher.Dispatcher()
dispatcher.map("/reply", receive_handler)
client.send_message("/readcals", [-2450, 380, 1.07, 43.5])
client.send_message("/writecals", [-1580.96399105604, 1521.8917381770461, 1.0200483825997142, 89.0, 276.0])
client.send_message("/readcals", [-2450, 380, 1.07, 43.5])
server = osc_server.ThreadingOSCUDPServer(("0.0.0.0", 7555), dispatcher)
print("Listening for OSC messages on port 5000...")
server.serve_forever()