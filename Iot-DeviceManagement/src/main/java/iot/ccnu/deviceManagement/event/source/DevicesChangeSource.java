package iot.ccnu.deviceManagement.event.source;


import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

public class DevicesChangeSource {
//    private FluxSink<Message<DevicesChangedEvent>> eventSink;
//    private Flux<Message<DevicesChangedEvent>> flux;
//
//    public DevicesChangeSource() {
//        this.flux = Flux.<Message<DevicesChangedEvent>>create(sink->this.eventSink=sink).publish().autoConnect();
//    }
//    private Mono<Void> publishDevicesChange(String operation, String message){
//        DevicesChangedEvent originalevent =  new DevicesChangedEvent(
//                DevicesChangedEvent.class.getTypeName(),
//                operation,
//                message);
//        Mono<DevicesChangedEvent> monoEvent = Mono.just(originalevent);
//        return monoEvent.map(event -> eventSink.next(MessageBuilder.withPayload(event).build())).then();
//    }
//    public Mono<Void> publishDevicesAddEvent(String message){
//        return publishDevicesChange("ADD",message);
//    }
//    public Mono<Void> publishDevicesUpdateEvent(String message){
//        return publishDevicesChange("UPDATE",message);
//    }
//    public Mono<Void> publishDevicesDeleteEvent(String message){
//        return publishDevicesChange("DELETE",message);
//    }
//


}
