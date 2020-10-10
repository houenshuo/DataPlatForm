package iot.ccnu.dataCollect.mqttServer.api.server.handler;

import com.google.common.collect.Lists;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.server.path.TopicManager;

import java.util.List;

public class MemoryTopicManager implements RsocketTopicManager {


    private TopicManager topicManager = new TopicManager();


    public List<TransportConnection> getConnectionsByTopic(String topic) {
        return topicManager.getTopicConnection(topic).orElse(Lists.newArrayList());
    }


    public void addTopicConnection(String topic, TransportConnection connection) {
        topicManager.addTopicConnection(topic,connection);
    }


    public void deleteTopicConnection(String topic, TransportConnection connection) {
        topicManager.deleteTopicConnection(topic,connection);
    }

    @Override
    public List<String> getAllTopics() {
        return null;
    }

}
