namespace net {
    void begin();
    void loop();

    void useHostname(const char* hostname);
    const char* hostname();
    void useStaticIP(char* ip, char* gateway, char* subnet);
    void useDHCP();
    bool isUp();
}