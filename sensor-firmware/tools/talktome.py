from pythonosc import udp_client
import requests

client = udp_client.SimpleUDPClient("esp32-osc-test.local", 7321)
client.send_message("/talktome", 7321)
client.send_message("/talktome", "/test")

r = requests.get('http://esp32-osc-test.local/cfg')
print(r.text)
