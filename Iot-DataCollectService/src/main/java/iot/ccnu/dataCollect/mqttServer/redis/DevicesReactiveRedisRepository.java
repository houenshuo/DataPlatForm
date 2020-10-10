package iot.ccnu.dataCollect.mqttServer.redis;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface DevicesReactiveRedisRepository {
    Mono<Long> addDevicesID(String value);
    Mono<Long> deleteDevicesID(String value);
    Flux<String> getAllDevicesID();
    Mono<Boolean> isDevicesID(String value);
}
