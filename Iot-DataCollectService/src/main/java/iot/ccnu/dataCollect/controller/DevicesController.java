package iot.ccnu.dataCollect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class DevicesController {
    @GetMapping("/v1")
    public String saveDevices1(){
        System.out.println("ok");
        return "ok";
    }
}
