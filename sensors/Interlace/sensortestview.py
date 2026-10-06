from pythonosc import osc_message_builder
from pythonosc import udp_client
from pythonosc import dispatcher
from pythonosc import osc_server
import numpy as np
import cv2
import socket
import processdat 
dat = np.empty((0, 2))
img=np.zeros((700,700,3), np.uint8)
# Send OSC packet
#ip_address = socket.gethostbyname("interlaced2")
ip_address="192.168.1.103"
print("IP address of 'interlace1':", ip_address)
#client = udp_client.SimpleUDPClient("192.168.68.115", 7321)
client = udp_client.SimpleUDPClient(ip_address, 7321)
maxX=0
maxY=0
minX=0
minY=0
xval=0
yval=0
# Receive OSC response
def receive_handlerx(address, *args):
    global img
    global maxX
    global minX
    global xval
    global yval
    if args[0]>maxX:
        maxX=args[0]
    if args[0]<minX:
        minX=args[0]
    xval=int((args[0]-minX)/(maxX-minX)*600)+50
    img[xval,yval]=[255,255,255]
    cv2.imshow('image',img)
    # show image until any key is pressed
    
    cv2.waitKey(1)
    
    

    # display opencv image
    #print(f"Received OSC message: {address} {args}")
def receive_handlery(address, *args):
    global img
    global maxY
    global minY
    global yval
    if args[0]>maxY:
        maxY=args[0]
    if args[0]<minY:
        minY=args[0]
    yval=int((args[0]-minY)/(maxY-minY)*600)+50
    #print(f"Received OSC message: {address} {args}")
def receive_handlerz(address, *args):
    global dat
    
    #print(f"Received OSC message: {address} {args}")

dispatcher = dispatcher.Dispatcher()
dispatcher.map("/lx/modulation/Mag1/magx", receive_handlerx)
dispatcher.map("/lx/modulation/Mag1/magy", receive_handlery)
dispatcher.map("/lx/modulation/Mag1/magz", receive_handlerz)
#client.send_message("/calibrate", "get cal data")
#client.send_message("/calibrate", [2000,10])#parameters are number of points and delay between points
server = osc_server.ThreadingOSCUDPServer(("0.0.0.0", 3030), dispatcher)
print("Listening for OSC messages on port 5000...")
server.serve_forever()
# run a periodic task
while True:
    print('hello')
print('done')