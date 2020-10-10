package iot.ccnu.dataCollect.mqttServer.container;

public interface ExceptorAcceptor {

    void accept(Throwable throwable);

}
