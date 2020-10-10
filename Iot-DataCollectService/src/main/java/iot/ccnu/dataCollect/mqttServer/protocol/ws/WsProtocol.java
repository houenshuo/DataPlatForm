package iot.ccnu.dataCollect.mqttServer.protocol.ws;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.protocol.Protocol;
import iot.ccnu.dataCollect.mqttServer.protocol.ProtocolTransport;

import java.util.List;

public class WsProtocol implements Protocol {


    @Override
    public boolean support(ProtocolType protocolType) {
        return protocolType == ProtocolType.WS_MQTT;
    }

    @Override
    public ProtocolTransport getTransport() {
        return  new WsTransport(this);
    }

    @Override
    public List<ChannelHandler> getHandlers() {
        return Lists.newArrayList( new HttpServerCodec(),
                new HttpObjectAggregator(65536),
                new WebSocketServerProtocolHandler("/", "mqtt, mqttv3.1, mqttv3.1.1"),
                new WebSocketFrameToByteBufDecoder(),
                new ByteBufToWebSocketFrameEncoder(),
                new MqttDecoder(5*1024*1024), MqttEncoder.INSTANCE);
    }
}
