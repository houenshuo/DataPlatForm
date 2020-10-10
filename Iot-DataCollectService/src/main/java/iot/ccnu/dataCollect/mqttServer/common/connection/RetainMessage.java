package iot.ccnu.dataCollect.mqttServer.common.connection;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * 遗留消息
 */
public class RetainMessage {
    private boolean dup;
    private boolean retain;
    private int qos;
    private String topicName;
    private byte[] copyByteBuf;
}
