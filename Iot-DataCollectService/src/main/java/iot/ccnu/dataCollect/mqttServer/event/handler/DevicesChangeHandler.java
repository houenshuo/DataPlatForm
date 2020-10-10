package iot.ccnu.dataCollect.mqttServer.event.handler;

import iot.ccnu.bean.eventModel.DevicesChangedEvent;
import iot.ccnu.dataCollect.mqttServer.event.DevicesChangeChannel;
import iot.ccnu.dataCollect.mqttServer.redis.DevicesReactiveRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

@EnableBinding(DevicesChangeChannel.class)
public class DevicesChangeHandler {
    @Autowired
    DevicesReactiveRedisRepository devicesReactiveRedisRepository;

    @StreamListener("inboundDevicesChanges")
    public void DevicesChangeSink(Message<DevicesChangedEvent> devicesChangedEvent){
        // redis 操作
        if(devicesChangedEvent.getPayload().getOperation().equals("DELETE")) {
            DevicesChangedEvent event =devicesChangedEvent.getPayload();
            devicesReactiveRedisRepository.deleteDevicesID(event.getMessage()).subscribe(m->System.out.println("删除设备"+devicesChangedEvent.getPayload().getMessage()));

        } else if(devicesChangedEvent.getPayload().getOperation().equals("ADD")){
            DevicesChangedEvent event =devicesChangedEvent.getPayload();
            devicesReactiveRedisRepository.addDevicesID(event.getMessage()).subscribe(m->System.out.println("添加设备"+devicesChangedEvent.getPayload().getMessage()));
        }
    }
}
