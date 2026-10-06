#include <Arduino.h>
//#define ARDUINOOSC_DEBUGLOG_ENABLE
#include <Wire.h>
#include <Adafruit_LIS3MDL.h>
#include <Adafruit_Sensor.h>
#include <ETH.h>
#include <ESPmDNS.h>
#include <ArduinoOSCETH.h>
#include <Preferences.h>
// Ethernet stuff
String target="/lx/modulation/Angles/angle3";
const char* hostname="interlaced3";
const IPAddress ip(192, 168, 1, 105);
const IPAddress gateway (192, 168,1, 1);
const IPAddress subnet (255, 255, 255, 0);
Adafruit_LIS3MDL lis3mdl;
// for ArduinoOSC
const char* host = "192.168.1.110";
const int recv_port = 7321;
const int bind_port = 7345;
const int send_port = 7555;
const int publish_port = 8000;  // OSCPlay default input port
// send / receive varibales
int i;
float f;
String s;
float xoffset;
float yoffset;
float xyscale;
float angmin;
float angmax;
float x;
float y;
float z;
Preferences preferences;


void calibrate(OscMessage& m) {
  for(int q=0;q<m.arg<int>(0);q++){
      lis3mdl.read(); 
        float x=lis3mdl.x;
        float y = lis3mdl.y;
        String s = "hello";
        Serial.println(q);
        OscEther.send(m.remoteIP(), send_port, "/reply", x, y, s,q);
        delay(m.arg<int>(1));
  }
  OscEther.send(m.remoteIP(), send_port, "/reply", 0.0, 0.0, "done");
}
void readcals(OscMessage& m) {
  xoffset= preferences.getFloat("xoffset",0);
  yoffset= preferences.getFloat("yoffset",0);
  xyscale=preferences.getFloat("xyscale",1);
  angmin=preferences.getFloat("angmin",0);
  angmax=preferences.getFloat("angmax",360);
  OscEther.send(m.remoteIP(), send_port, "/reply", xoffset, yoffset,xyscale,angmin,angmax, "done");
  Serial.println(xoffset);
  Serial.println(yoffset);
  Serial.println(xyscale);
   Serial.println(angmin);
    Serial.println(angmax);
  Serial.println("read out cals");
}
void writecals(OscMessage& m) {
preferences.putFloat("xoffset",m.arg<float>(0));
preferences.putFloat("yoffset",m.arg<float>(1));
preferences.putFloat("xyscale",m.arg<float>(2));
preferences.putFloat("angmin",m.arg<float>(3));
preferences.putFloat("angmax",m.arg<float>(4));
  OscEther.send(m.remoteIP(), send_port, "/reply", 0.0, 0.0, "done");
  xoffset= preferences.getFloat("xoffset",0);
  yoffset= preferences.getFloat("yoffset",0);
  xyscale=preferences.getFloat("xyscale",1);
  angmin=preferences.getFloat("angmin",0);
  angmax=preferences.getFloat("angmax",360);

  Serial.println("wrote out cals");
}


void setup() {

  preferences.begin("calvals", false);
  
  xoffset= preferences.getFloat("xoffset",0);
  yoffset= preferences.getFloat("yoffset",0);
   xyscale=preferences.getFloat("xyscale",1);
 angmin=preferences.getFloat("angmin",0);
 angmax=preferences.getFloat("angmax",1);
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
  lis3mdl.setIntThreshold(500);
  lis3mdl.configInterrupt(false, false, true, // enable z axis
                          true, // polarity
                          false, // don't latch
                          true); // enabled!
    // Ethernet stuff
    ETH.begin();
    
    ETH.config(ip, gateway, subnet);
    ETH.setHostname(hostname);
    ETH.enableIPv6();
    
    
  if (!MDNS.begin("interlaced2")) {
    Serial.println("Error setting up MDNS responder!");
    while (1) {
      delay(1000);
    }
  }

  Serial.println(ETH.localIP());
  Serial.println("mDNS responder started");
  delay(2000);
  Serial.println(MDNS.queryHost("Art_Bucket", 15000).toString());
Serial.println(MDNS.queryHost("SPIDERTRAP", 15000).toString());
    // publish osc messages (default publish rate = 30 [Hz])
    //OscEther.publish(host, publish_port, "/lx/mixer/crossfader", f)
    //    ->setFrameRate(60.f);

    OscEther.publish(host, publish_port, target, f)
        ->setFrameRate(60.f);

    OscEther.subscribe(recv_port, "/need/reply", []() {
        Serial.println("/need/reply");

        int i = millis();
        float f = (float)micros() / 1000.f;
        String s = "hello";

        OscEther.send(host, send_port, "/reply", i, f, s);
    });

    
     OscEther.subscribe(recv_port, "/calibrate", calibrate);
     OscEther.subscribe(recv_port, "/readcals", readcals);
     OscEther.subscribe(recv_port, "/writecals", writecals);
}

void loop() {
lis3mdl.read(); 
float rate=0.9;
x=rate*x+(1-rate)*lis3mdl.x;
y=rate*y+(1-rate)*lis3mdl.y;
z=rate*z+(1-rate)*lis3mdl.z;

  //f=atan2((y-yoffset)/xyscale,(x-xoffset))/3.14/2+0.5;
  f=(int(360+360*atan2((y-yoffset)/xyscale,(x-xoffset))/3.14159/2)%360-angmin)/(angmax-angmin);
  f=constrain(f, 0, 1);

  delay(8);

    if (ETH.linkUp() && ETH.localIP() != IPAddress(0, 0, 0, 0)) {
    OscEther.update();  // should be called to receive + send osc
  }

}
