#include <Arduino.h>

#include "net.h"
#include "osc.h"
// #include "ota.h" // WiFi only
#include "web.h"
#include "sensor.h"

void setup() {
    Serial.begin(115200);
    delay(1000);
    Serial.println("Starting...");

    net::begin();
    osc::begin();
    // ota::begin(); // WiFi only
    sensor::begin();
    web::begin();
}

void loop() {
    net::loop();
    osc::loop();
    //   ota::loop(); // WiFi only
    web::loop();
    sensor::loop();
  // Forced context switch for background tasks
    vTaskDelay(1);
}
