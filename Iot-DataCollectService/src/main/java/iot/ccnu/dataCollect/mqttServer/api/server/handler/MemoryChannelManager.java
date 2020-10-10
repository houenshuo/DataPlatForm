package iot.ccnu.dataCollect.mqttServer.api.server.handler;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.redis.DevicesReactiveRedisRepositoryImp;
import iot.ccnu.dataCollect.mqttServer.utils.SpringUtil;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
public class MemoryChannelManager implements RsocketChannelManager {


    private CopyOnWriteArrayList<TransportConnection> connections = new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String, TransportConnection> connectionMap = new ConcurrentHashMap<>();

    private DevicesReactiveRedisRepositoryImp devicesReactiveRedisRepositoryImp = SpringUtil.getBean(DevicesReactiveRedisRepositoryImp.class);


    public MemoryChannelManager() {
        //每次从设备模块 读取最新的设备编号 刷新redis
    }

    @Override
    public List<TransportConnection> getConnections() {
        return connections;
    }

    @Override
    public void addConnections(TransportConnection connection) {
        connections.add(connection);
    }

    @Override
    public void removeConnections(TransportConnection connection) {
        connections.remove(connection);
        //移除redis
        //通知设备管理模块 设备下线
    }

    @Override
    public void addDeviceId(String deviceId, TransportConnection connection) {
        connectionMap.put(deviceId,connection);
        //
    }

    @Override
    public void removeDeviceId(String deviceId) {
        connectionMap.remove(deviceId);
    }

    @Override
    public TransportConnection getRemoveDeviceId(String deviceId) {
        return connectionMap.remove(deviceId);
    }

    @Override
    public Mono<Boolean> checkDeviceId(String deviceId) {
        return devicesReactiveRedisRepositoryImp.isDevicesID(deviceId);
    }


}
