#include <ESPAsyncWebServer.h>

namespace osc {
    void begin();
    void loop();

    void setTarget(const char* host, uint16_t port, const char* osc_address);
    void webcfg(AsyncResponseStream *response);
}