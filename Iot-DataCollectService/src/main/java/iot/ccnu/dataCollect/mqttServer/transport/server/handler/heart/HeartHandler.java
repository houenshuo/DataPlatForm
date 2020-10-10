package iot.ccnu.dataCollect.mqttServer.transport.server.handler.heart;

import io.netty.handler.codec.mqtt.MqttMessage;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.config.RsocketConfiguration;
import iot.ccnu.dataCollect.mqttServer.transport.DirectHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartHandler implements DirectHandler {


    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        switch (message.fixedHeader().messageType()){
            case PINGREQ:
                //处理客户端发来的心跳请求
                System.out.println("客户端发来心跳请求");
                connection.sendPingRes().subscribe();
            case PINGRESP:
        }

    }
}
