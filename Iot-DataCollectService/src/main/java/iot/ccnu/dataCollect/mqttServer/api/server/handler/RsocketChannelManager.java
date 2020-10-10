package iot.ccnu.dataCollect.mqttServer.api.server.handler;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 设备-连接  管理
 */
public interface RsocketChannelManager  {

    List<TransportConnection> getConnections();

    void  addConnections(TransportConnection connection);


    void removeConnections(TransportConnection connection);

    void addDeviceId(String deviceId, TransportConnection connection);

    void removeDeviceId(String deviceId);

    TransportConnection getRemoveDeviceId(String deviceId);

    Mono<Boolean> checkDeviceId(String deviceId);
}
