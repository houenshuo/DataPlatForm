package iot.ccnu.dataCollect.mqttServer.transport.client.handler.heart;

import io.netty.handler.codec.mqtt.MqttMessage;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.config.RsocketConfiguration;
import iot.ccnu.dataCollect.mqttServer.transport.DirectHandler;

public class HeartHandler implements DirectHandler {
    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        switch (message.fixedHeader().messageType()){
            case PINGRESP:
                System.out.println("server端发来的心跳回复");
                break;
        }

    }
}
