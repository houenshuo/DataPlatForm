package iot.ccnu.dataCollect.mqttServer.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
@Repository
public class DevicesReactiveRedisRepositoryImp implements DevicesReactiveRedisRepository{
    @Autowired
    private ReactiveRedisTemplate<String,String> reactiveRedisTemplate;
    private static final String SET_NAME="DeviceID:";

    @Override
    public Mono<Long> addDevicesID(String value) {
        System.out.println(value);
        return reactiveRedisTemplate.opsForSet().add(SET_NAME,value);
    }

    @Override
    public Mono<Long> deleteDevicesID(String value) {
        return reactiveRedisTemplate.opsForSet().remove(SET_NAME,value);
    }

    @Override
    public Flux<String> getAllDevicesID() {
        return reactiveRedisTemplate.opsForSet().members(SET_NAME);
    }

    @Override
    public Mono<Boolean> isDevicesID(String value) {// 判断是否存在
        return reactiveRedisTemplate.opsForSet().isMember(SET_NAME,value);
    }
}
