package iot.ccnu.dataCollect.mqttServer.mongo.service;

import iot.ccnu.dataCollect.mqttServer.mongo.Repositories.SensorRepository;
import iot.ccnu.dataCollect.mqttServer.mongo.model.Sensors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SensorService {
    @Autowired
    private SensorRepository sensorRepository;

    public Mono<Long> save(Sensors sensors){
        return sensorRepository.save(sensors);
    }
}
