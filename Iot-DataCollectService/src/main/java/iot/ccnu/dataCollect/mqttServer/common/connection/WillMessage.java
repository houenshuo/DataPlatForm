package iot.ccnu.dataCollect.mqttServer.common.connection;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WillMessage {
    private int qos;// 服务质量等级
    private String topicName;
    private byte[] copyByteBuf;
    private boolean retain;
}
