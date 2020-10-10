package iot.ccnu.dataCollect.mqttServer.transport.server.handler;

import io.netty.handler.codec.mqtt.MqttMessageType;
import iot.ccnu.dataCollect.mqttServer.common.exception.NotSuppportHandlerException;
import iot.ccnu.dataCollect.mqttServer.transport.DirectHandler;
import iot.ccnu.dataCollect.mqttServer.transport.server.handler.connect.ConnectHandler;
import iot.ccnu.dataCollect.mqttServer.transport.server.handler.heart.HeartHandler;
import iot.ccnu.dataCollect.mqttServer.transport.server.handler.pub.PubHandler;
import iot.ccnu.dataCollect.mqttServer.transport.server.handler.sub.SubHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息
 */
public class DirectHandlerFactory {
    private final MqttMessageType messageType;// 控制报文的类型

    private ConcurrentHashMap<MqttMessageType, DirectHandler> messageTypeCollection = new ConcurrentHashMap();

    public DirectHandlerFactory(MqttMessageType messageType) {
        this.messageType = messageType;
    }

    public DirectHandler loadHandler(){
        // 处理client发来的相关报文
        return messageTypeCollection.computeIfAbsent(messageType,type->{
            switch (type){
                case PUBACK://qos1 消息发布收到确认
                case PUBREC://发布收到（保证交付第一步）
                case PUBREL:
                case PUBLISH:
                case PUBCOMP://qos2消息发布完成
                    return new PubHandler();

                case CONNECT:// 客户端请求连接服务端
                case DISCONNECT://客户端断开连接
                    return new ConnectHandler();

                case PINGREQ://心跳请求
                    return new HeartHandler();

                case SUBSCRIBE://客户端订阅：保存订阅者的主题和client端的连接，当接受到发布者的主题相关消息，以便转发订阅者消息
                case UNSUBSCRIBE://客户端取消订阅
                    return new SubHandler();
            }
            throw  new NotSuppportHandlerException(messageType+" not support ");
        });
    }
}
