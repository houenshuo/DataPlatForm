package iot.ccnu.dataCollect.mqttServer.transport.server.connection;

import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.Attribute;
import iot.ccnu.dataCollect.mqttServer.api.AttributeKeys;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.server.RsocketServerSession;
import iot.ccnu.dataCollect.mqttServer.api.server.handler.*;
import iot.ccnu.dataCollect.mqttServer.config.RsocketServerConfig;
import iot.ccnu.dataCollect.mqttServer.transport.server.handler.ServerMessageRouter;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.Connection;
import reactor.netty.DisposableChannel;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * server： 保存、处理client端的连接、主题等
 */
public class RsocketServerConnection implements RsocketServerSession {
    private DisposableServer disposableServer;

    private RsocketChannelManager channelManager;

    private RsocketTopicManager topicManager;

    private RsocketMessageHandler rsocketMessageHandler;

    private RsocketServerConfig config;

    private ServerMessageRouter messageRouter;

    private DisposableServer wsDisposableServer;

    public RsocketServerConnection(UnicastProcessor<TransportConnection> connections, DisposableServer server, DisposableServer wsDisposableServer, RsocketServerConfig config) {
        this.disposableServer = server;
        this.config = config;
        this.rsocketMessageHandler = config.getMessageHandler();
        this.topicManager = Optional.ofNullable(config.getTopicManager()).orElse(new MemoryTopicManager());
        this.channelManager = Optional.ofNullable(config.getChannelManager()).orElse(new MemoryChannelManager());
        this.messageRouter = new ServerMessageRouter(config);
        this.wsDisposableServer = wsDisposableServer;
        //Subscribe a Consumer to this Flux that will consume all the elements in the sequence. It will request an unbounded demand (Long.MAX_VALUE)
        //处理器 数据源来自：新的客户端连接对象（客户端连接服务器都会生成一个）
        connections.subscribe(this::subscribe);
    }

    /**
     * 处理器订阅的接口实现
     *  处理server端生成的新的客户端连接对象
     *
     * @param connection
     */
    private void subscribe(TransportConnection connection) {

        NettyInbound inbound = connection.getInbound();// 对某client的读入
        Connection c = connection.getConnection();

        Disposable disposable = Mono.fromRunnable(c::dispose)// 定时关闭
                .delaySubscription(Duration.ofSeconds(10))
                .subscribe();

        c.channel().attr(AttributeKeys.connectionAttributeKey).set(connection); // 设置connection
        c.channel().attr(AttributeKeys.closeConnection).set(disposable);   // 设置close
        //监听到client的连接关闭时：向订阅will主题发送消息
        connection.getConnection().onDispose(() -> {
            Optional.ofNullable(connection.getConnection().channel().attr(AttributeKeys.WILL_MESSAGE)).map(Attribute::get)
                    .ifPresent(willMessage -> Optional.ofNullable(topicManager.getConnectionsByTopic(willMessage.getTopicName()))
                            .ifPresent(connections -> connections.forEach(co -> {
                                MqttQoS qoS = MqttQoS.valueOf(willMessage.getQos());
                                switch (qoS) {
                                    case AT_LEAST_ONCE:
                                        co.sendMessage(false, qoS, willMessage.isRetain(), willMessage.getTopicName(), willMessage.getCopyByteBuf()).subscribe();
                                        break;
                                    case EXACTLY_ONCE:
                                    case AT_MOST_ONCE:
                                        co.sendMessageRetry(false, qoS, willMessage.isRetain(), willMessage.getTopicName(), willMessage.getCopyByteBuf()).subscribe();
                                        break;
                                    default:
                                        co.sendMessage(false, qoS, willMessage.isRetain(), willMessage.getTopicName(), willMessage.getCopyByteBuf()).subscribe();
                                        break;
                                }
                            })));
            closeConnection(connection);
        });
        /**
         * 读事件
         *  client要先来发 连接报文
         */
        try {
            inbound.receiveObject().cast(MqttMessage.class).doOnError((message)->{//ava.io.IOException: 远程主机强迫关闭了一个现有的连接
                System.out.println(connection.getConnection().channel().attr(AttributeKeys.device_id)+"客户端关闭了");
            }).subscribe(message -> messageRouter.handler(message, connection));
        }catch (Exception e){
            System.out.println("远程客户端出现了强制关闭或者其他异常关闭，server读数据失败");
        }
    }

    /**
     * 删除远程连接对象 订阅 等
     * @param connection
     */
    private void closeConnection(TransportConnection connection){
        System.out.println(connection.getConnection().channel().attr(AttributeKeys.device_id)+"通知下线了");
        channelManager.removeConnections(connection); // 删除远程连接对象
        connection.getTopics().forEach(topic -> topicManager.deleteTopicConnection(topic, connection)); // 删除订阅主题下的此远程连接对象
        Optional.ofNullable(connection.getConnection().channel().attr(AttributeKeys.device_id))
                .map(Attribute::get)
                .ifPresent(channelManager::removeDeviceId); // 设置device Id
        connection.destory();
        System.out.println("当前客户端连接个数"+channelManager.getConnections().size());

    }
    @Override
    public Mono<List<TransportConnection>> getConnections() {
        return Mono.just(channelManager.getConnections());
    }

    @Override
    public Mono<Void> closeConnect(String clientId) {
        return Mono.fromRunnable(()->Optional.ofNullable(channelManager.getRemoveDeviceId(clientId))
                .ifPresent(TransportConnection::dispose));
    }
    @Override
    public void dispose() {
        disposableServer.dispose();
        Optional.ofNullable(wsDisposableServer)
                .ifPresent(DisposableChannel::dispose);
    }
}

