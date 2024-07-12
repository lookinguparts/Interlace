#include <Arduino.h>
#include <ETH.h>
#include <ArduinoOTA.h>
#include <FS.h>
#include <SPIFFS.h>

#include "ota.h"
#include "net.h"

void ota::begin() {
  ArduinoOTA
    .setHostname(net::hostname())
    .onStart([]() {
      String type;
      if (ArduinoOTA.getCommand() == U_FLASH)
        type = "sketch";
      else // U_SPIFFS
        type = "filesystem";
        SPIFFS.end();

      Serial.println("Start OTA updating " + type);
    })
    .onEnd([]() {
      Serial.println("\n OTA End");
    })
    .onProgress([](unsigned int progress, unsigned int total) {
      Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
    })
    .onError([](ota_error_t error) {
      Serial.printf("Error[%u]: ", error);
      if (error == OTA_AUTH_ERROR) Serial.println("Auth Failed");
      else if (error == OTA_BEGIN_ERROR) Serial.println("Begin Failed");
      else if (error == OTA_CONNECT_ERROR) Serial.println("Connect Failed");
      else if (error == OTA_RECEIVE_ERROR) Serial.println("Receive Failed");
      else if (error == OTA_END_ERROR) Serial.println("End Failed");
    })
    .begin();
}

void ota::loop() {
  ArduinoOTA.handle();
}