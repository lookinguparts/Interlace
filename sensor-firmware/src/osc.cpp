#include <ETH.h>
#include <ESPmDNS.h>
//#define ARDUINOOSC_DEBUGLOG_ENABLE

#include <ArduinoOSCETH.h>
#include <Preferences.h>

#include "osc.h"
#include "net.h"
#include "sensor.h"

const int recv_port = 7321;
const float frame_rate = 30;

std::string targethost = "";
int16_t targetport = 0;
std::string targetpath = "";

void loadTarget();
void saveTarget();

OscPublishElementRef publisher;

void startSender() {
    if (targethost.empty() || targetport == 0 || targetpath.empty()) {
        return;
    }
    OscEther.publish(targethost.c_str(), targetport, targetpath.c_str(), &sensor::latestValue)
        ->setFrameRate(frame_rate);
}

void osc::begin() {
    MDNS.addService("osc", "udp", recv_port);

    loadTarget();
    startSender();

    OscEther.subscribe(recv_port, "/cfg/hostname", [](String& s) {
        Serial.print("/cfg/hostname: ");
        Serial.println(s);
        net::useHostname(s.c_str());
    });

    OscEther.subscribe(recv_port, "/log", [](String& s) {
        Serial.print("/log: ");
        Serial.println(s);
    });

    OscEther.subscribe(recv_port, "/talktome", [](OscMessage& m) {
        Serial.print(m.remoteIP());
        Serial.print(" ");
        Serial.print(m.remotePort());
        Serial.print(" ");
        Serial.print(m.size());
        Serial.print(" ");
        Serial.print(m.address());

        targethost = m.remoteIP().c_str();
        if (m.isInt32(0)) {
            targetport = m.arg<int>(0);
            Serial.print(" ");
            Serial.print(m.arg<int>(0));
        } else if (m.isStr(1)) {
            targetpath = m.arg<String>(1).c_str();
            Serial.print(" ");
            Serial.print(m.arg<String>(1));
        }
        if (m.isStr(0)) {
            targetpath = m.arg<String>(0).c_str();
            Serial.print(" ");
            Serial.print(m.arg<String>(0));
        }
        Serial.println();
        saveTarget();
        startSender();
    });

}

void osc::webcfg(AsyncResponseStream *response) {
    response->printf("osc_target_host=%s\n", targethost.c_str());
    response->printf("osc_target_port=%d\n", targetport);
    response->printf("osc_target_path=%s\n", targetpath.c_str());
}

void osc::loop() {
    if (net::isUp()) {
        OscEther.update();
    }
}

Preferences preferences;

void loadTarget() {
  if (!preferences.begin("osc", true)) {
    Serial.println("failed to open OSC preferences");
    return;
  };
  char buf[64];
  preferences.getString("host", buf, 63);
  targethost = buf;
  targetport = preferences.getUShort("port", targetport);
  preferences.getString("path", buf, 63);
  targetpath = buf;
  preferences.end();
}

void saveTarget() {
  preferences.begin("osc", false);
  preferences.putString("host", targethost.c_str());
  preferences.putUShort("port", targetport);
  preferences.putString("path", targetpath.c_str());
  preferences.end();
}
