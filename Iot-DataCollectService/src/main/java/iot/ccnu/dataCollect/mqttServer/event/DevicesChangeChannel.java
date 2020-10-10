package iot.ccnu.dataCollect.mqttServer.event;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface DevicesChangeChannel {
    @Input("inboundDevicesChanges")
    SubscribableChannel devicesChangeChannel();
}
