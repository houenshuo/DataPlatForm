package iot.ccnu.dataCollect.mqttServer.protocol;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.config.RsocketConfiguration;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.DisposableServer;
/**
 * 协议传输 抽象类
 *      mqtt、websocket等等
 */
public abstract class ProtocolTransport {
    protected Protocol protocol;// 支持的协议类型

    public ProtocolTransport(Protocol protocol){
        this.protocol=protocol;
    }

    public abstract Mono<? extends DisposableServer> start(RsocketConfiguration config, UnicastProcessor<TransportConnection> connections);

    public abstract Mono<TransportConnection>  connect(RsocketConfiguration config);

}
