package iot.ccnu.dataCollect.mqttServer.transport.client.handler;

import io.netty.handler.codec.mqtt.MqttMessageType;
import iot.ccnu.dataCollect.mqttServer.common.exception.NotSuppportHandlerException;
import iot.ccnu.dataCollect.mqttServer.transport.DirectHandler;
import iot.ccnu.dataCollect.mqttServer.transport.client.handler.connect.ConnectHandler;
import iot.ccnu.dataCollect.mqttServer.transport.client.handler.heart.HeartHandler;
import iot.ccnu.dataCollect.mqttServer.transport.client.handler.pub.PubHandler;
import iot.ccnu.dataCollect.mqttServer.transport.client.handler.sub.SubHandler;


import java.util.concurrent.ConcurrentHashMap;

public class DirectHandlerFactory {

    private final MqttMessageType messageType;

    private ConcurrentHashMap<MqttMessageType, DirectHandler> messageTypeCollection = new ConcurrentHashMap();

    public DirectHandlerFactory(MqttMessageType messageType) {
        this.messageType = messageType;
    }

    public DirectHandler loadHandler(){
        // 处理server发来的相关报文
        return messageTypeCollection.computeIfAbsent(messageType,type->{
            switch (type){
                case PUBACK:
                case PUBREC:
                case PUBREL:
                case PUBLISH:
                case PUBCOMP:
                    return new PubHandler();

                case CONNACK://连接报文确认
                    return new ConnectHandler();

                case PINGRESP:
                    return new HeartHandler();

                case UNSUBACK:// 取消订阅报文确认
                case SUBACK://订阅请求报文确认
                    return new SubHandler();
            }
            throw  new NotSuppportHandlerException(messageType+" not support ");
        });
    }


}
