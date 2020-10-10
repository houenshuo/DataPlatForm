package iot.ccnu.dataCollect.mqttServer.transport.server.handler.pub;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.*;
import iot.ccnu.dataCollect.mqttServer.api.MqttMessageApi;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.config.RsocketConfiguration;
import iot.ccnu.dataCollect.mqttServer.common.message.TransportMessage;
import iot.ccnu.dataCollect.mqttServer.config.RsocketServerConfig;
import iot.ccnu.dataCollect.mqttServer.transport.DirectHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
@Slf4j
/**
 * qos 消息发布 相关处理器
 */
public class PubHandler implements DirectHandler {


    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        RsocketServerConfig serverConfig = (RsocketServerConfig) config;
        MqttFixedHeader header = message.fixedHeader();//报文头
        switch (header.messageType()) {
            case PUBLISH:
                MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) message;
                MqttPublishVariableHeader variableHeader = mqttPublishMessage.variableHeader();
                ByteBuf byteBuf = mqttPublishMessage.payload();
                byte[] bytes = copyByteBuf(byteBuf);
                if (header.isRetain()) {//保留消息
                    serverConfig.getMessageHandler().saveRetain(header.isDup(), header.isRetain(), header.qosLevel().value(), variableHeader.topicName(), bytes);
                }
                switch (header.qosLevel()) {
                    // 处理发布者pub发来mqtt消息
                    // 根据主题 转发消息给 订阅者客户端
                    case AT_MOST_ONCE:
                        System.out.println("服务器转发消息给消费者订阅的主题Qos0："+variableHeader.topicName());
                        serverConfig.getTopicManager().getConnectionsByTopic(variableHeader.topicName())//查找主题
                                .stream()//.filter(c->!c.isDispose())// 过滤掉本身 已经关闭的dispose
                                .forEach(c -> {
                                    if (c.isDispose()){
                                        System.out.println("消费者客户端 关闭了");
                                    }else {
                                        System.out.println("服务器转发消息给消费者订阅的主题Qos0："+variableHeader.topicName()+c.isDispose());
                                        c.sendMessage(false, header.qosLevel(), header.isRetain(), variableHeader.topicName(),bytes).subscribe();
                                    }
                                    });
                        break;
                    case AT_LEAST_ONCE:
                        MqttPubAckMessage mqttPubAckMessage = MqttMessageApi.buildPuback(header.isDup(), header.qosLevel(), header.isRetain(), variableHeader.packetId()); // back
                        connection.write(mqttPubAckMessage).subscribe();
                        serverConfig.getTopicManager().getConnectionsByTopic(variableHeader.topicName())
                                .stream().filter(c->!connection.equals(c) && !c.isDispose())
                                .forEach(c -> {
                                    System.out.println("服务器转发消息给消费者订阅的主题Qos1："+variableHeader.topicName());
                                    c.sendMessageRetry(false, header.qosLevel(), header.isRetain(), variableHeader.topicName(),bytes).subscribe();}
                                );
                        break;
                    case EXACTLY_ONCE:
                        int messageId = variableHeader.packetId();
                        MqttPubAckMessage mqttPubRecMessage = MqttMessageApi.buildPubRec(messageId);
                        connection.write(mqttPubRecMessage).subscribe();
                        connection.addDisposable(messageId, Mono.fromRunnable(() ->
                                connection.write(MqttMessageApi.buildPubRel(messageId)).subscribe())
                                .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
                        TransportMessage transportMessage = TransportMessage.builder().isRetain(header.isRetain())
                                .isDup(false)
                                .topic(variableHeader.topicName())
                                .message(bytes)
                                .qos(header.qosLevel().value())
                                .build();
                        connection.saveQos2Message(messageId, transportMessage);
                        break;
                    case FAILURE:
                        log.error(" publish FAILURE {} {} ", header, variableHeader);
                        break;
                }
                break;
            case PUBACK:
                MqttMessageIdVariableHeader Back = (MqttMessageIdVariableHeader) message.variableHeader();
                connection.cancelDisposable(Back.messageId());
                break;
            case PUBREC:
                MqttMessageIdVariableHeader recVH = (MqttMessageIdVariableHeader) message.variableHeader();
                int id = recVH.messageId();
                connection.cancelDisposable(id);
                connection.write(MqttMessageApi.buildPubRel(id)).subscribe();  //  send rel
                connection.addDisposable(id, Mono.fromRunnable(() ->
                        connection.write(MqttMessageApi.buildPubRel(id)).subscribe())
                        .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
                break;
            case PUBREL:
                MqttMessageIdVariableHeader rel = (MqttMessageIdVariableHeader) message.variableHeader();
                int messageId = rel.messageId();
                connection.cancelDisposable(messageId); // cancel replay rec
                MqttPubAckMessage mqttPubRecMessage = MqttMessageApi.buildPubComp(messageId);
                connection.write(mqttPubRecMessage).subscribe();  //  send comp
                connection.getAndRemoveQos2Message(messageId)
                        .ifPresent(msg -> serverConfig.getTopicManager().getConnectionsByTopic(msg.getTopic())
                                .stream().filter(c->!connection.equals(c) && !c.isDispose())
                                .forEach(c -> c.sendMessageRetry(false, MqttQoS.valueOf(msg.getQos()), header.isRetain(), msg.getTopic(),msg.getMessage()).subscribe()));
                break;
            case PUBCOMP:
                MqttMessageIdVariableHeader compVH = (MqttMessageIdVariableHeader) message.variableHeader();
                connection.cancelDisposable(compVH.messageId());
                break;
        }
    }



    private byte[] copyByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}
