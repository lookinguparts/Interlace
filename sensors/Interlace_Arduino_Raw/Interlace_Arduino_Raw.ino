#include <Arduino.h>
//#define ARDUINOOSC_DEBUGLOG_ENABLE
#include <Wire.h>
#include <Adafruit_LIS3MDL.h>
#include <Adafruit_Sensor.h>
#include <ETH.h>
#include <ArduinoOSCETH.h>

// Raw magnetometer streamer.  Unlike Interlace_Arduino.ino this applies no
// calibration, filtering or angle math -- it just publishes the uncorrected
// LIS3MDL counts so they can be captured and fit on the Pi.

// Per-unit network config.  Change these for each ESP32.
String target = "/mag1/xyz";
const char* hostname = "interlace1";
const IPAddress ip(192, 168, 1, 105);
const IPAddress gateway(192, 168, 1, 1);
const IPAddress subnet(255, 255, 255, 0);

Adafruit_LIS3MDL lis3mdl;

// for ArduinoOSC
const char* host = "192.168.1.110";
const int publish_port = 8000;  // OSCPlay default input port

// Published variables.  ArduinoOSC binds these by reference, so updating them
// in loop() is all that is needed to change what gets sent.
float x;
float y;
float z;

void setup() {
  Serial.begin(115200);
  delay(2000);
  Serial.println("Starting...");

  if (! lis3mdl.begin_I2C()) {
    Serial.println("Failed to find LIS3MDL chip");
    while (1) { delay(10); }
  }
  Serial.println("LIS3MDL Found!");
  lis3mdl.setPerformanceMode(LIS3MDL_MEDIUMMODE);
  lis3mdl.setOperationMode(LIS3MDL_CONTINUOUSMODE);
  lis3mdl.setDataRate(LIS3MDL_DATARATE_80_HZ);
  lis3mdl.setRange(LIS3MDL_RANGE_4_GAUSS);

  // Ethernet stuff
  ETH.begin();
  ETH.config(ip, gateway, subnet);
  ETH.setHostname(hostname);
  ETH.enableIPv6();

  Serial.println(ETH.localIP());

  // Publish raw x, y, z as a single OSC message.  80 Hz matches the sensor
  // output data rate configured above.
  OscEther.publish(host, publish_port, target, x, y, z)
      ->setFrameRate(80.f);
}

void loop() {
  lis3mdl.read();
  x = lis3mdl.x;
  y = lis3mdl.y;
  z = lis3mdl.z;

  delay(8);

  if (ETH.linkUp() && ETH.localIP() != IPAddress(0, 0, 0, 0)) {
    OscEther.update();  // should be called to receive + send osc
  }
}
