package iot.ccnu.dataCollect.mqttServer.container;

public interface MessageAcceptor {

    void accept(String topic, byte[] message);

}
