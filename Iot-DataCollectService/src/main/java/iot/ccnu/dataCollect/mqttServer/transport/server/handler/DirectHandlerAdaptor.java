package iot.ccnu.dataCollect.mqttServer.transport.server.handler;

import io.netty.handler.codec.mqtt.MqttMessageType;

public interface DirectHandlerAdaptor {
    DirectHandlerFactory handler(MqttMessageType messageType);

}
