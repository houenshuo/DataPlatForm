package iot.ccnu.dataCollect.mqttServer.api.server;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * server服务端会话 接口
 */
public interface RsocketServerSession extends Disposable {


    Mono<List<TransportConnection>> getConnections();

    Mono<Void> closeConnect(String clientId);

}
