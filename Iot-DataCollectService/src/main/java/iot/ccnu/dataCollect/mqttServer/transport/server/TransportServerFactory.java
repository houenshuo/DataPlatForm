package iot.ccnu.dataCollect.mqttServer.transport.server;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.server.RsocketServerSession;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.config.RsocketServerConfig;
import iot.ccnu.dataCollect.mqttServer.protocol.ProtocolFactory;
import iot.ccnu.dataCollect.mqttServer.protocol.ws.WsProtocol;
import iot.ccnu.dataCollect.mqttServer.protocol.ws.WsTransport;
import iot.ccnu.dataCollect.mqttServer.transport.server.connection.RsocketServerConnection;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.DisposableServer;
/**
 * 服务端server 工厂
 */
public class TransportServerFactory {

    private ProtocolFactory protocolFactory;//协议工厂：websocker协议、mqtt协议
    //单传播处理器：multiple producers and only one consumer
    private UnicastProcessor<TransportConnection> unicastProcessor =UnicastProcessor.create();// 发送客户端的连接TransportConnection
    //server端 mqtt配置参数
    private RsocketServerConfig config;
    //netty底层服务器的上下文信息 资源
    private DisposableServer ws_server;

    public TransportServerFactory(){
        protocolFactory = new ProtocolFactory();
    }


    /**
     * 返回数据流RsocketServerSession的Mono对象
     * @param config
     * @return
     */
    public Mono<RsocketServerSession> start(RsocketServerConfig config) {
        this.config =config;
        if(config.getProtocol() == ProtocolType.MQTT.name()){ // 开启 ws协议
            WsTransport wsTransport = new WsTransport(new WsProtocol());
            RsocketServerConfig wsConfig=copy(config);
            ws_server=wsTransport.start(wsConfig,unicastProcessor).block();
        }
        return  Mono.from(
                protocolFactory.getProtocol(ProtocolType.valueOf(config.getProtocol()))
                        .get().getTransport()
                        .start(config,unicastProcessor))//创建TCP server服务端
                .map(this::wrapper)// server的处理包装RsocketServerConnection
                .doOnError(config.getThrowableConsumer());
    }

    private RsocketServerConfig copy(RsocketServerConfig config) {
        RsocketServerConfig serverConfig = new RsocketServerConfig();
        serverConfig.setThrowableConsumer(config.getThrowableConsumer());
        serverConfig.setLog(config.isLog());
        serverConfig.setMessageHandler(config.getMessageHandler());
        serverConfig.setAuth(config.getAuth());
        serverConfig.setChannelManager(config.getChannelManager());
        serverConfig.setIp(config.getIp());
        serverConfig.setPort(8443);// 设置固定的端口号 地址+1883  客户端要连接这个地址
        serverConfig.setSsl(config.isSsl());
        serverConfig.setProtocol(ProtocolType.WS_MQTT.name());
        serverConfig.setHeart(config.getHeart());
        serverConfig.setTopicManager(config.getTopicManager());
        serverConfig.setRevBufSize(config.getRevBufSize());
        serverConfig.setSendBufSize(config.getSendBufSize());
        serverConfig.setNoDelay(config.isNoDelay());
        serverConfig.setKeepAlive(config.isKeepAlive());
        return  serverConfig;
    }

    private  RsocketServerSession wrapper(DisposableServer server){
        return  new RsocketServerConnection(unicastProcessor,server,ws_server,config);
    }




}
