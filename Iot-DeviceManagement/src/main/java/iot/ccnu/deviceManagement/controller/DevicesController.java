package iot.ccnu.deviceManagement.controller;

import iot.ccnu.deviceManagement.mode.Devices;
import iot.ccnu.deviceManagement.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class DevicesController {
    @Autowired
    private DeviceService deviceService;
    @PostMapping("/v1/addDevices")
    public String saveDevices(@RequestBody Devices devices){
        deviceService.saveDevices(devices);
        return "ok";
    }
    @PostMapping("/v1/updateDevices")
    public void updateDevices(Devices devices){
        deviceService.updateDevices(devices);
    }
    @PostMapping("/v1/Devices/{uuid}")
    public void deleteDevices(@PathVariable("uuid")String uuid){
        //deviceService.deleteDevices(uuid);
    }

    @GetMapping("/v1")
    public String saveDevices1(){
        deviceService.saveDevices();
        System.out.println("ok");
        return "ok";
    }
    @GetMapping("/v2")
    public String saveDevices2(){
        deviceService.deleteDevices();
        System.out.println("ok");
        return "ok";
    }
}
