#include <Arduino.h>
#include <ETH.h>
#include <ESPmDNS.h>

#include "net.h"

std::string hostname = HOSTNAME;

void net::begin() {
    delay(500);
    Serial.printf("setup as \"%s\"\n", ::hostname.c_str());
    //ETH.setHostname(::hostname.c_str());
    ETH.setHostname(::hostname.c_str());
    //ETH.begin(ip, gateway, subnet);
    ETH.begin();
    ETH.enableIpV6(); /// must be after ETH.begin()

    if (!MDNS.begin(::hostname.c_str())) {
        Serial.println("Error setting up MDNS responder!");
    }
    Serial.println("network enabled, waiting to initialize...");
    for (int i = 0; i < 20; i++) {
        if (isUp()) { break; }
        vTaskDelay(500 / portTICK_PERIOD_MS);

        Serial.print(".");
    }
    Serial.println(ETH.localIP().toString());
    Serial.println(ETH.localIPv6().toString());
}

void net::loop() {
}

const char* net::hostname() {
    return ::hostname.c_str();
}

void net::useHostname(const char* newname) {
    ::hostname.assign(newname);
    ETH.setHostname(newname);
    MDNS.end();
    MDNS.begin(newname);
}

void net::useStaticIP(char* ip, char* gateway, char* subnet) {
    //ETH.begin(ip, gateway, subnet);
}

void net::useDHCP() {
    ETH.begin();
}

bool net::isUp() {
    return ETH.linkUp() && ETH.localIP() != IPAddress(0, 0, 0, 0);
}