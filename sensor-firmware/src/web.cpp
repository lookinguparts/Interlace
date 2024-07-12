#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <AsyncOTA.h>

#include <FS.h> //this needs to be first, or it all crashes and burns...
#include "SPIFFS.h"

#include "web.h"
#include "net.h"
#include "osc.h"
#include "sensor.h"

AsyncWebServer http_server(80);

void web::begin() {

	if (SPIFFS.begin()) {
		Serial.println("SPIFFS MOUNTED");
	}
	else {
		Serial.println("SPIFFS MOUNT FAIL");
	}

    http_server.on("/varz", HTTP_GET, [](AsyncWebServerRequest *request) {
        AsyncResponseStream *response = request->beginResponseStream("text/plain");
        response->printf("heap_free: %d\n", ESP.getFreeHeap());
        sensor::varz(response);
        request->send(response);
    });

    http_server.on("/cfg", HTTP_GET, [](AsyncWebServerRequest *request) {
        AsyncResponseStream *response = request->beginResponseStream("text/plain");
        response->printf("hostname=%s\n", net::hostname());
        osc::webcfg(response);
        sensor::webcfg(response);
        request->send(response);
    });

    http_server.on("/", HTTP_GET, [](AsyncWebServerRequest *request) {
        request->send(SPIFFS, "/www/index.html", "text/html");
    });

    http_server.serveStatic("/", SPIFFS, "/www/");

    AsyncOTA.begin(&http_server);

    // Start server
    http_server.begin();
}

AsyncWebServer* web::server() {
   return &http_server;
}

void web::loop() {
}