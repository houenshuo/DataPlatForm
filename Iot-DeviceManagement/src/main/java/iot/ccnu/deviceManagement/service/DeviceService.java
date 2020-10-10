package iot.ccnu.deviceManagement.service;

import iot.ccnu.bean.eventModel.DevicesChangedEvent;
import iot.ccnu.deviceManagement.event.source.DevicesChangeSource;
import iot.ccnu.deviceManagement.event.source.DevicesSource;
import iot.ccnu.deviceManagement.mode.Devices;
import iot.ccnu.deviceManagement.repository.DevicesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class DeviceService {
//    @Autowired
//    private DevicesRepository devicesRepository;
//    @Autowired
//    private DevicesChangeSource devicesChangeSource;
    @Autowired
    private DevicesSource devicesSource;

//    public Mono<Devices> saveDevices(Devices devices){
//        Mono<Devices> devicesMono=null;
//        Mono<Void> voidMono =devicesChangeSource.publishDevicesAddEvent(devices.getUuid());
//        return devicesMono;
//    }
//    public Mono<Void> updateDevices(Devices devices){
//        Mono<Devices> mono =null;
//        Mono<Void> voidMono =devicesChangeSource.publishDevicesUpdateEvent(devices.getUuid());
//        return Mono.when(mono,voidMono);
//    }
//    public Mono<Void> deleteDevices(Devices devices){
//        Mono<Void> deleteDevices =null;
//        Mono<Void> voidMono =devicesChangeSource.publishDevicesDeleteEvent(devices.getUuid());
//        return Mono.when(deleteDevices,voidMono);
//    }
    public Devices saveDevices(Devices devices){
        Devices devicesMono=null;
        //发消息给消费者
       // devicesChangeSource.publishDevicesAddEvent(devices.getUuid());
        //保存数据库
        //devicesRepository.save(devices);
        return devicesMono;
    }
    public void saveDevices(){
        System.out.println("ceshi -------------------------------");
        DevicesChangedEvent event =new DevicesChangedEvent(DevicesChangedEvent.class.getTypeName(),"ADD","Produce1");
        devicesSource.send(event);
    }
    public void updateDevices(Devices devices){
        //devicesRepository.save(devices);
    }
    public void deleteDevices(){
       // devicesChangeSource.publishDevicesDeleteEvent(uuid);

        //Optional<Devices> devices1 =devicesRepository.findById(uuid);
        //devicesRepository.delete(devices1.get());
        System.out.println("shanchu -------------------------------");
        DevicesChangedEvent event =new DevicesChangedEvent(DevicesChangedEvent.class.getTypeName(),"DELETE","Produce");
        devicesSource.send(event);
    }


}
