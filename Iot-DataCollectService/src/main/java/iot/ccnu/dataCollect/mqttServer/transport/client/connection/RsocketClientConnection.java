package iot.ccnu.dataCollect.mqttServer.transport.client.connection;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import iot.ccnu.dataCollect.mqttServer.api.AttributeKeys;
import iot.ccnu.dataCollect.mqttServer.api.MqttMessageApi;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.client.RsocketClientSession;
import iot.ccnu.dataCollect.mqttServer.config.RsocketClientConfig;
import iot.ccnu.dataCollect.mqttServer.transport.client.handler.ClientMessageRouter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
@Slf4j
/**
 * client端 mqtt的报文传输
 *      对TransportConnection的封装
 */
public class RsocketClientConnection implements RsocketClientSession {

    private final TransportConnection connection;//mqtt协议传输连接

    private final RsocketClientConfig clientConfig;

    private ClientMessageRouter clientMessageRouter;//client消息转发中心


    public RsocketClientConnection(TransportConnection connection, RsocketClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.connection = connection;
        this.clientMessageRouter = new ClientMessageRouter(clientConfig);

        initHandler();//客户端首次连接、重连初始化
    }

    public void  initHandler(){
        RsocketClientConfig.Options options = clientConfig.getOptions();
        NettyInbound inbound = connection.getInbound();//获取读数据的能力
        //客户端请求连接服务端 报文
        connection.write(MqttMessageApi.buildConnect(
                options.getClientIdentifier(),
                options.getWillTopic(),
                options.getWillMessage(),
                options.getUserName(),
                options.getPassword(),
                options.isHasUserName(),
                options.isHasPassword(),
                options.isHasWillFlag(),
                options.getWillQos(),
                clientConfig.getHeart()
        )).doOnError(throwable -> log.error(throwable.getMessage())).subscribe();

        connection.getConnection().channel().attr(AttributeKeys.closeConnection).set(Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildConnect(
                options.getClientIdentifier(),//id
                options.getWillTopic(),
                options.getWillMessage(),
                options.getUserName(),//用户名
                options.getPassword(),
                options.isHasUserName(),
                options.isHasPassword(),
                options.isHasWillFlag(),
                options.getWillQos(),
                clientConfig.getHeart()
        )).subscribe()).delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());

        connection.getConnection().onWriteIdle(clientConfig.getHeart(),
                () -> connection.sendPingReq().subscribe()); // 向服务器发送心跳
//        connection.getConnection().onReadIdle(clientConfig.getHeart()*2,
//                () -> connection.sendPingReq().subscribe()); // 发送心跳
        // client端 正常关闭
        connection.getConnection().onDispose(()->clientConfig.getOnClose().run());

        /**
         *读事件
         *  接受server发来的mqtt协议报文，并处理
         */
        inbound.receiveObject().cast(MqttMessage.class)
                .subscribe(message ->  clientMessageRouter.handler(message, connection));

        connection.getConnection().channel().attr(AttributeKeys.clientConnectionAttributeKey).set(this);

        /**
         * 客户端断开重连 恢复主题订阅等
         */
        //根据当前所有的主题创建对应的订阅：MqttTopicSubscription包含主题过滤器和最大的服务质量等级
        System.out.println(clientConfig.getOptions().getClientIdentifier()+"如果客户端不是首次启动，会恢复之前的订阅主题，重新订阅以下的主题集合"+connection.getTopics().toString());
        List<MqttTopicSubscription> mqttTopicSubscriptions=connection.getTopics()
                .stream()
                .map(s -> new MqttTopicSubscription(s, MqttQoS.AT_MOST_ONCE))//创建订阅
                .collect(Collectors.toList());

        if(mqttTopicSubscriptions!=null && mqttTopicSubscriptions.size()>0){
            int messageId = connection.messageId();
            //保存当前客户端所有的 订阅集合
            connection.addDisposable(messageId, Mono.fromRunnable(() ->
                    connection.write(MqttMessageApi.buildSub(messageId, mqttTopicSubscriptions)).subscribe())
                    .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retryPooledConnectionProvider
            //客户端发送订阅请求 控制报文
            connection.write(MqttMessageApi.buildSub(messageId, mqttTopicSubscriptions)).subscribe();
        }
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, boolean retained, int qos) {
        int messageId = qos == 0 ? 1 : connection.messageId();
        MqttQoS mqttQoS = MqttQoS.valueOf(qos);
        //发布消息 控制报文  双向
        switch (mqttQoS) {
            case AT_MOST_ONCE:
                return connection.write(MqttMessageApi.buildPub(false, MqttQoS.AT_MOST_ONCE, retained, messageId, topic, Unpooled.wrappedBuffer(message)));
            case EXACTLY_ONCE:
            case AT_LEAST_ONCE:
                return Mono.fromRunnable(()->{
                    connection.write(MqttMessageApi.buildPub(false, mqttQoS, retained, messageId, topic, Unpooled.wrappedBuffer(message))).subscribe();
                    connection.addDisposable(messageId, Mono.fromRunnable(() ->
                            connection.write(MqttMessageApi.buildPub(true,mqttQoS, retained, messageId, topic, Unpooled.wrappedBuffer(message))).subscribe())
                            .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
                });
            default:
                return  Mono.empty();
        }
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message) {
        return pub(topic, message, false, 0);
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, int qos) {
        return pub(topic, message, false, qos);
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, boolean retained) {
        return pub(topic, message, retained, 0);
    }


    @Override
    public Mono<Void> sub(String... subMessages) {
        connection.getTopics().addAll(Arrays.asList(subMessages));
        List<MqttTopicSubscription> topicSubscriptions = Arrays.stream(subMessages)
                .map(s -> new MqttTopicSubscription(s, MqttQoS.AT_MOST_ONCE))
                .collect(Collectors.toList());
        int messageId = connection.messageId();
        //?为什么重新发送
        connection.addDisposable(messageId, Mono.fromRunnable(() ->
                connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions)).subscribe())
                .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
        System.out.println(clientConfig.getOptions().getClientIdentifier()+"发送订阅请求"+subMessages.toString());
        //客户端发送订阅请求 控制报文
        return connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions));
    }


    @Override
    public Mono<Void> unsub(List<String> topics) {
        connection.getTopics().removeAll(Arrays.asList(topics));
        int messageId = connection.messageId();
        connection.addDisposable(messageId, Mono.fromRunnable(() ->
                connection.write(MqttMessageApi.buildUnSub(messageId, topics)).subscribe())
                .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
        //客户端取消订阅请求 控制报文
        return connection.write(MqttMessageApi.buildUnSub(messageId, topics));
    }

    @Override
    public Mono<Void> unsub() {
        return unsub(connection.getTopics());
    }

    @Override
    public Mono<Void> messageAcceptor(BiConsumer<String, byte[]> messageAcceptor) {
        return Mono.fromRunnable(() -> clientConfig.setMessageAcceptor(messageAcceptor));
    }

    @Override
    public void dispose() {
        connection.dispose();
    }
}

