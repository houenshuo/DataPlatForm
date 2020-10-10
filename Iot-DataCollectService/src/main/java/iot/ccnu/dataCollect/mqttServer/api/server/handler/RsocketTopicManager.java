package iot.ccnu.dataCollect.mqttServer.api.server.handler;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import org.apache.kafka.common.protocol.types.Field;

import java.util.List;

/**
 * 主题-消息 管理
 */
public interface RsocketTopicManager {
    List<TransportConnection> getConnectionsByTopic(String topic);//根据主题获取所有的 传输连接
    void  addTopicConnection(String topic, TransportConnection connection);
    void  deleteTopicConnection(String topic, TransportConnection connection);
    List<String> getAllTopics();
}
