package iot.ccnu.dataCollect.mqttServer.transport.client.handler.sub;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.config.RsocketConfiguration;
import iot.ccnu.dataCollect.mqttServer.transport.DirectHandler;

public class SubHandler implements DirectHandler {


    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        MqttFixedHeader header=  message.fixedHeader();
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader =(MqttMessageIdVariableHeader) message.variableHeader();
        switch (header.messageType()){
            case  SUBACK:
            case UNSUBACK:
                connection.cancelDisposable(mqttMessageIdVariableHeader.messageId());
                break;
        }
    }
}
