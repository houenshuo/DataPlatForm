package iot.ccnu.dataCollect.mqttServer.transport.client;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.client.RsocketClientSession;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.config.RsocketClientConfig;
import iot.ccnu.dataCollect.mqttServer.protocol.ProtocolFactory;
import iot.ccnu.dataCollect.mqttServer.transport.client.connection.RsocketClientConnection;
import reactor.core.publisher.Mono;

public class TransportClientFactory {
    private ProtocolFactory protocolFactory;
    private RsocketClientConfig clientConfig;
    public TransportClientFactory(){
        protocolFactory = new ProtocolFactory();
    }
    public Mono<RsocketClientSession> connect(RsocketClientConfig config) {
        this.clientConfig=config;
        return  Mono.from(protocolFactory.getProtocol(ProtocolType.valueOf(config.getProtocol()))
                .get().getTransport().connect(config))//获得TransportConnection(具有mqtt协议报文传输能力)
                .map(this::wrapper)//包装成client端的mqtt协议报文传输
                .doOnError(config.getThrowableConsumer());
    }


    private RsocketClientSession wrapper(TransportConnection connection){
        return  new RsocketClientConnection(connection,clientConfig);
    }
}
