package iot.ccnu.dataCollect.mqttServer.transport.client.handler;

import io.netty.handler.codec.mqtt.MqttMessageType;

public interface DirectHandlerAdaptor {
    DirectHandlerFactory handler(MqttMessageType messageType);
}
