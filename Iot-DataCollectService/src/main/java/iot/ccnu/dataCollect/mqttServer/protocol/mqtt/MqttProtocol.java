package iot.ccnu.dataCollect.mqttServer.protocol.mqtt;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.protocol.Protocol;
import iot.ccnu.dataCollect.mqttServer.protocol.ProtocolTransport;

import java.util.List;


public class MqttProtocol implements Protocol {


    @Override
    public boolean support(ProtocolType protocolType) {
        return protocolType == ProtocolType.MQTT;
    }

    @Override
    public ProtocolTransport getTransport() {
        return  new MqttTransport(this);//创建mqtt传输对象，依赖此对象内部的方法完成传输
    }

    // ChannelHandler事件处理 mqtt的解码器编码器
    @Override
    public List<ChannelHandler> getHandlers() {
        return Lists.newArrayList( new MqttDecoder(5*1024*1024), MqttEncoder.INSTANCE);
    }
}
