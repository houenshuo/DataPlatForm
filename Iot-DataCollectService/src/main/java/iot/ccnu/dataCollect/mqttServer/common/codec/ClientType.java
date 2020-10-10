package iot.ccnu.dataCollect.mqttServer.common.codec;

import lombok.Getter;

@Getter
public enum ClientType {
    Web((byte)1), //网页
    Android((byte)2), //安卓
    Ios((byte)3), // 苹果
    Other((byte)4); // 其他

    private  byte type;
    ClientType(byte type) {
        this.type = type;
    }
}
