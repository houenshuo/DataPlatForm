package iot.ccnu.dataCollect;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.server.RsocketServerSession;
import iot.ccnu.dataCollect.mqttServer.api.server.handler.MemoryMessageHandler;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.transport.server.TransportServer;
import iot.ccnu.dataCollect.mqttServer.utils.SpringUtil;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.List;


@SpringBootApplication
public class IotDataCollectServeMain implements ApplicationRunner {


    public static void main(String[] args)  {
        ConfigurableApplicationContext context =SpringApplication.run(IotDataCollectServeMain.class,args);
//        System.out.println("111");
//        SpringUtil.applicationContext =context;
//        RsocketServerSession serverSession= TransportServer.create("localhost",9999)
//                .auth((s,p)->true)
//                .heart(100000)
//                .protocol(ProtocolType.MQTT)
//                .ssl(false)
//                .auth((username,password)->true)
//                .log(false)
//                .messageHandler(new MemoryMessageHandler())
//                .exception(throwable -> System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+throwable))
//                .start()//启动消息代理服务
//                .block();
//        serverSession.closeConnect("device-1").subscribe();// 关闭设备端
//        List<TransportConnection> connections= serverSession.getConnections().block(); // 获取所有链接
//        System.out.println("sdaaaaaaaaaaa");
    }

    @Bean
    public ReactiveRedisConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory("localhost", 6379);
    }
    @Bean
    ReactiveRedisTemplate<String,String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory){
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
