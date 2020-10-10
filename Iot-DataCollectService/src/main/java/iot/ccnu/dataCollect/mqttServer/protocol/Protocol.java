package iot.ccnu.dataCollect.mqttServer.protocol;

import io.netty.channel.ChannelHandler;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;

import java.util.List;

public interface Protocol {

    boolean support(ProtocolType protocolType);//验证支持

    ProtocolTransport getTransport();//获取协议传输

    List<ChannelHandler> getHandlers();

}

