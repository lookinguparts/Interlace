#include <ESPAsyncWebServer.h>

namespace sensor {
    struct RawData {
        float temp_c;
        float mag_x;
        float mag_y;
        float mag_z;
    };

    void begin();
    void loop();

    RawData latestRaw();
    float latestValue();

    void varz(AsyncResponseStream *response);
    void webcfg(AsyncResponseStream *response);
}