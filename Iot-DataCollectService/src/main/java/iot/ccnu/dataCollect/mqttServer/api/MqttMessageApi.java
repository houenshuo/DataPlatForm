package iot.ccnu.dataCollect.mqttServer.api;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.*;

import java.util.List;

/**
 * netty的实现的mqtt 控制报文
 * mqtt规定了14种控制报文：固定报头、可以变报头、有效载荷部分 组成
 * 具体的类型在MqttMessageType定义
 */
public class MqttMessageApi {
    public static MqttPublishMessage buildPub(boolean isDup, MqttQoS qoS, boolean isRetain, int messageId, String topic, ByteBuf message){
        //固定报头
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH,isDup, qoS,isRetain,0);
        //有效载荷部分
        MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(topic,messageId);
        MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader,mqttPublishVariableHeader, message);
        return  mqttPublishMessage;
    }


    public static MqttPubAckMessage buildPuback(boolean isDup, MqttQoS qoS, boolean isRetain, int messageId){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK,isDup, qoS,isRetain,2);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(mqttFixedHeader,mqttMessageIdVariableHeader);
        return  mqttPubAckMessage;
    }

    public static  MqttPubAckMessage buildPubRec(int messageId){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC,false, MqttQoS.AT_LEAST_ONCE,false,0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(mqttFixedHeader,from);
    }

    public static  MqttPubAckMessage buildPubRel(int messageId){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL,false, MqttQoS.AT_LEAST_ONCE,false,0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(mqttFixedHeader,from);
    }


    public static  MqttPubAckMessage buildPubComp(int messageId){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP,false, MqttQoS.AT_MOST_ONCE,false,0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(mqttFixedHeader,from);
    }


    public  static  MqttSubAckMessage buildSubAck(int messageId, List<Integer> qos){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttSubAckPayload payload = new MqttSubAckPayload(qos);
        return new MqttSubAckMessage(mqttFixedHeader, variableHeader, payload);
    }


    public static  MqttUnsubAckMessage buildUnsubAck(int messageId){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0x02);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttUnsubAckMessage(mqttFixedHeader, variableHeader);
    }

    public  static  MqttConnAckMessage buildConnectAck(MqttConnectReturnCode connectReturnCode){
        MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(connectReturnCode,true);
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
                MqttMessageType.CONNACK,false, MqttQoS.AT_MOST_ONCE, false, 0x02);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    public static  MqttSubscribeMessage buildSub(int messageId,List<MqttTopicSubscription> topicSubscriptions){
        MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(topicSubscriptions);
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE,false, MqttQoS.AT_LEAST_ONCE,false,0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader =MqttMessageIdVariableHeader.from(messageId);
        return new MqttSubscribeMessage(mqttFixedHeader,mqttMessageIdVariableHeader,mqttSubscribePayload);
    }

    public static  MqttUnsubscribeMessage buildUnSub(int messageId,List<String> topics){
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE,false, MqttQoS.AT_LEAST_ONCE,false,0x02);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttUnsubscribePayload MqttUnsubscribeMessage = new MqttUnsubscribePayload(topics);
        return new MqttUnsubscribeMessage(mqttFixedHeader,variableHeader,MqttUnsubscribeMessage);
    }

    /**
     * 客户端请求连接服务端
     */
    public static MqttConnectMessage buildConnect(String clientId, String willTopic, String willMessage, String username, String password, boolean isUsername, boolean isPassword, boolean isWill, int willQos, int heart){
        MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(MqttVersion.MQTT_3_1_1.protocolName(),MqttVersion.MQTT_3_1_1.protocolLevel(),isUsername,isPassword,false,willQos,isWill,false,heart);
        MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(clientId,willTopic,isWill?willMessage.getBytes():null,username,isPassword?password.getBytes():null);
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT,false, MqttQoS.AT_MOST_ONCE,false,10);
        return new MqttConnectMessage(mqttFixedHeader,mqttConnectVariableHeader,mqttConnectPayload);
    }
}
