package iot.ccnu.dataCollect.mqttServer.container;

public interface AuthencationSession {
    boolean auth(String username, String password);
}
