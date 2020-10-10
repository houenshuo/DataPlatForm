package iot.ccnu.dataCollect.mqttServer.transport;

import io.netty.handler.codec.mqtt.MqttMessage;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.config.RsocketConfiguration;

/**
 * 消息转发控制器 接口
 */
public interface DirectHandler {
    void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config);
}