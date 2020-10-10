package iot.ccnu.dataCollect.mqttServer.api.server.handler;

import iot.ccnu.dataCollect.mqttServer.common.connection.RetainMessage;

import java.util.Optional;

/**
 * 遗嘱消息 管理
 */
public interface RsocketMessageHandler {
    void saveRetain(boolean dup, boolean retain, int qos, String topicName, byte[] copyByteBuf);

    Optional<RetainMessage> getRetain(String topicName);
}

