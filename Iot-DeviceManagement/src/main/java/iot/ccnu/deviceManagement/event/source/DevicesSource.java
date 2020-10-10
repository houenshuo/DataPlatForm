package iot.ccnu.deviceManagement.event.source;

import iot.ccnu.bean.eventModel.DevicesChangedEvent;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import javax.annotation.Resource;
import java.util.UUID;

@EnableBinding(Source.class) //定义消息的推送管道
public class DevicesSource {
    @Resource
    private MessageChannel output; // 消息发送管道

    public String send(DevicesChangedEvent event)
    {
        output.send(MessageBuilder.withPayload(event).build());
        System.out.println("*****serial: ");
        return null;
    }
    public String delete()
    {
        String serial = UUID.randomUUID().toString();
        output.send(MessageBuilder.withPayload(serial).build());
        System.out.println("*****serial: "+serial);
        return null;
    }
}
