package iot.ccnu.dataCollect.mqttServer.container;

import iot.ccnu.dataCollect.mqttServer.api.client.RsocketClientSession;
import iot.ccnu.dataCollect.mqttServer.api.server.RsocketServerSession;
import iot.ccnu.dataCollect.mqttServer.api.server.handler.MemoryMessageHandler;
import iot.ccnu.dataCollect.mqttServer.api.server.handler.RsocketMessageHandler;
import iot.ccnu.dataCollect.mqttServer.transport.client.TransportClient;
import iot.ccnu.dataCollect.mqttServer.transport.server.TransportServer;
import iot.ccnu.dataCollect.mqttServer.utils.SpringUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Configuration
//开启自动配置，注册一个IdGeneratorProperties类型的配置bean到spring容器，同普通的@EnableAsync等开关一样
@EnableConfigurationProperties(IotConfig.class)
public class IotConfiguration implements ApplicationContextAware{

    private ConfigurableApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        SpringUtil.applicationContext =this.applicationContext;
    }

    /**
     * mqtt服务器
     * @param iotConfig
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "iot.mqtt.server",name = "enable", havingValue = "true")
    public RsocketServerSession initServer(@Autowired IotConfig iotConfig)  {
        return TransportServer.create(iotConfig.getServer().getHost(),iotConfig.getServer().getPort())
                .heart(iotConfig.getServer().getHeart())
                .log(iotConfig.getServer().isLog())
                .protocol(iotConfig.getServer().getProtocol())
                .backlog(iotConfig.getServer().getBacklog())
                .revBufSize(iotConfig.getServer().getRevBufSize())
                .sendBufSize(iotConfig.getServer().getSendBufSize())
                .noDelay(iotConfig.getServer().isNoDelay())
                .keepAlive(iotConfig.getServer().isKeepAlive())
                .auth((s,p)->true)//可以查数据库 对设备的配置 进行验证
                .ssl(iotConfig.getServer().isSsl())
                .messageHandler(new MemoryMessageHandler())
                .exception(throwable -> System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+throwable))
                .start()
                .block();
    }

    @Bean
    @ConditionalOnProperty(prefix = "iot.mqtt.client",name = "enable", havingValue = "true")
    public RsocketClientSession initClient(@Autowired IotConfig iotConfig)  {
        IotConfig.Client client=iotConfig.getClient();
        RsocketClientSession rsocketClientSession=TransportClient.create(client.getIp(),client.getPort())
                .heart(client.getHeart())
                .protocol(client.getProtocol())
                .ssl(client.isSsl())
                .log(client.isLog())
                .clientId(client.getOption().getClientIdentifier())
                .password(client.getOption().getPassword())
                .username(client.getOption().getPassword())
                .willMessage(client.getOption().getWillMessage())
                .willTopic(client.getOption().getWillTopic())
                .willQos(client.getOption().getWillQos())
                .exception(throwable -> System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+throwable))
                .messageAcceptor((topic,msg)->{// 消息处理
                    // 存入消息中间件

                    System.out.println(topic+":"+new String(msg));
                })
                .connect()
                .block();
        rsocketClientSession.sub("consumer").subscribe();// 订阅主题
        return rsocketClientSession;
    }
}
