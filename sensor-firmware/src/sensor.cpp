#include <Arduino.h>

#include "sensor.h"

SemaphoreHandle_t sensorDataSemaphore;
sensor::RawData latest_data;

void sensor::begin() {
    Serial.println("sensor::begin()");
    sensorDataSemaphore = xSemaphoreCreateMutex();
    latest_data.temp_c = 40.0;
    latest_data.mag_x = 0.0;
    latest_data.mag_y = 0.0;
    latest_data.mag_z = 0.0;

}

void sensor::loop() {
   if (xSemaphoreTake(sensorDataSemaphore, 10/portTICK_PERIOD_MS)) {
        latest_data.mag_x = (millis() % 5000) / 5000.0;
        xSemaphoreGive(sensorDataSemaphore);
    }

}

void sensor::varz(AsyncResponseStream *response) {
    RawData data = sensor::latestRaw();
    response->printf("temp_c: %f\n", data.temp_c);
    response->printf("temp_f: %f\n", data.temp_c * 9.0 / 5.0 + 32.0);
    response->printf("mag_x: %f\n", data.mag_x);
    response->printf("mag_y: %f\n", data.mag_y);
    response->printf("mag_z: %f\n", data.mag_z);
}

void sensor::webcfg(AsyncResponseStream *response) {
}

float sensor::latestValue() {
    return sensor::latestRaw().mag_x;
}

sensor::RawData sensor::latestRaw() {
    RawData data;
    if (xSemaphoreTake(sensorDataSemaphore, 10/portTICK_PERIOD_MS)) {
        data = latest_data;
        xSemaphoreGive(sensorDataSemaphore);
    }
    return data;
}