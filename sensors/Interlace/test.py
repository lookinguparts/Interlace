from pythonosc import osc_message_builder
from pythonosc import udp_client
from pythonosc import dispatcher
from pythonosc import osc_server
import numpy as np
import cv2
import socket
import threading
import time
from collections import deque
import threading

# Shared data structure for points
points_lock = threading.Lock()
current_point = {'x': 0, 'y': 0}  # Current point position
point_updated = False  # Flag to indicate new data

ip_address = "192.168.1.103"
client = udp_client.SimpleUDPClient(ip_address, 7321)
maxX = 0
maxY = 0
minX = 0
minY = 0

def display_loop():
    # Initialize image only in display loop
    img = np.zeros((700,700,3), dtype=np.float32)
    cv2.namedWindow('image', cv2.WINDOW_AUTOSIZE)
    
    while True:
        # Apply decay
        img *= 0.999  # Decay by 0.1%
        
        # Update image with new point if available
        global point_updated
        with points_lock:
            if point_updated:
                img[current_point['x']-2:current_point['x']+2, current_point['y']-2:current_point['y']+2] = [1.0, 1.0, 1.0]
                point_updated = False
        
        # Convert to uint8 for display
        display_img = (np.clip(img, 0, 1) * 255).astype(np.uint8)
        
        cv2.imshow('image', display_img)
        if cv2.waitKey(10) & 0xFF == ord('q'):  # ~30 FPS refresh rate
            break

def receive_handlerx(address, *args):
    global maxX, minX, point_updated
    if args[0] > maxX:
        maxX = args[0]
    if args[0] < minX:
        minX = args[0]
    try:
        xval = int((args[0]-minX)/(maxX-minX)*600)+50
        with points_lock:
            current_point['x'] = xval
            point_updated = True
    except Exception as e:
        print(f"Error: {e}")

def receive_handlery(address, *args):
    global maxY, minY, point_updated
    if args[0] > maxY:
        maxY = args[0]
    if args[0] < minY:
        minY = args[0]
    try:
        yval = int((args[0]-minY)/(maxY-minY)*600)+50
        with points_lock:
            current_point['y'] = yval
            point_updated = True
    except Exception as e:
        print(f"Error: {e}")

def receive_handlerz(address, *args):
    pass

# Start display thread
display_thread = threading.Thread(target=display_loop)
display_thread.daemon = True
display_thread.start()

# Setup OSC server
dispatcher = dispatcher.Dispatcher()
dispatcher.map("/lx/modulation/Mag1/magx", receive_handlerx)
dispatcher.map("/lx/modulation/Mag1/magy", receive_handlery)
dispatcher.map("/lx/modulation/Mag1/magz", receive_handlerz)

server = osc_server.ThreadingOSCUDPServer(("0.0.0.0", 3030), dispatcher)
print("Listening for OSC messages on port 3030...")
server.serve_forever()