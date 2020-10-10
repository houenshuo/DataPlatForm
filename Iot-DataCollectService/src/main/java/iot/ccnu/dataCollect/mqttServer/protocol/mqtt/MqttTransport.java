package iot.ccnu.dataCollect.mqttServer.protocol.mqtt;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.Attribute;
import iot.ccnu.dataCollect.mqttServer.api.AttributeKeys;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.config.RsocketConfiguration;
import iot.ccnu.dataCollect.mqttServer.config.RsocketClientConfig;
import iot.ccnu.dataCollect.mqttServer.protocol.ProtocolTransport;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * tcp server和tcp client实现和增强，拥有mqtt报文传输的能力
 *
 * 1，基于reactor-netty框架中TCP server 和 TCP client 实现服务器的建立 和 客户端的连接
 *          其中Tcp server是消息代理服务器使用  TCP client是订阅者和发送者使用
 * 2，对TCP的connection对象进行扩展包装 加入mqtt协议的规范，得到TransportConnection：增加了mqtt报文的传输能力
 */
@Slf4j
public class MqttTransport extends ProtocolTransport {

    public MqttTransport(MqttProtocol mqttProtocol) {
        super(mqttProtocol);
    }


    @Override
    /**
     * start a TCP server
     * 参考https://projectreactor.io/docs/netty/release/reference/index.html#_starting_and_stopping官方文档
     */
    public Mono<? extends DisposableServer> start(RsocketConfiguration config, UnicastProcessor<TransportConnection> connections) {

        return buildServer(config)
                .doOnConnection(connection -> {// connection是客户端远程连接对象，对netty的channel的进一步封装，具体的实现由框架创建，在TcpServerDoOnConnection中调用此函数
                    protocol.getHandlers().forEach(connection::addHandlerLast);
                    //处理器手动发送信号，onNext是订阅者的方法，处理器中通过订阅者对象调用onNext发送信息
                    // 新的client连接  会在RsocketServerConnection的subscribe方法中处理
                    log.info("server端产生了 新的客户端连接--------");
                    connections.onNext(new TransportConnection(connection));
                })
                .bind()// 数据流类型DisposableServer
                .doOnError(config.getThrowableConsumer());
    }

    private TcpServer buildServer(RsocketConfiguration config) {
        TcpServer server = TcpServer.create()//创建
                .port(config.getPort())//Host and Port
                .wiretap(config.isLog())
                .host(config.getIp())
                // 设置 channelOption 更多选择参考netty
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_KEEPALIVE, config.isKeepAlive())
                .option(ChannelOption.TCP_NODELAY, config.isNoDelay())
                .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                .option(ChannelOption.SO_RCVBUF, config.getRevBufSize())
                .option(ChannelOption.SO_SNDBUF, config.getSendBufSize())
                ;
        return config.isSsl() ? server.secure(sslContextSpec -> sslContextSpec.sslContext(Objects.requireNonNull(buildContext()))) : server;

    }


    private SslContext buildContext() {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch (Exception e) {
            log.error("ssl 错误：", e.getMessage());
        }
        return null;
    }


    /**
     * tcp client
     */
    @Override
    public Mono<TransportConnection> connect(RsocketConfiguration config) {
        return buildClient(config)
                .connect()// 创建tcp客户端连接
                .map(connection -> {
                    Connection connection1= connection;
                    log.info(((RsocketClientConfig)config).getOptions().getClientIdentifier()+"客户端开启-----------");
                    protocol.getHandlers().forEach(connection1::addHandler);
                    TransportConnection transportConnection = new TransportConnection(connection);
                    // 客户端的监听connect关闭时，开始重连
                    connection.onDispose(() -> retryConnect(config,transportConnection));
                    return transportConnection;
                });
    }


    private void retryConnect(RsocketConfiguration config, TransportConnection transportConnection) {
        log.info(((RsocketClientConfig)config).getOptions().getClientIdentifier()+"客户端开始尝试重连中");
        buildClient(config)
                .connect()
                .doOnError(config.getThrowableConsumer())
                .retry()
                .cast(Connection.class)
                .subscribe(connection -> {
                    protocol.getHandlers().forEach(connection::addHandler);
                    Optional.ofNullable(transportConnection.getConnection().channel().attr(AttributeKeys.clientConnectionAttributeKey))// 把TransportConnection旧的connection关闭
                            .map(Attribute::get)
                            .ifPresent(rsocketClientSession ->{
                                // 对客户端的TransportConnection对象重新设置新的连接属性，对于server也会产生新的connection连接
                                transportConnection.setConnection(connection);
                                transportConnection.setInbound(connection.inbound());
                                transportConnection.setOutbound(connection.outbound());
                                rsocketClientSession.initHandler();
                                connection.onDispose(() -> retryConnect(config,transportConnection)); //嵌套超时重连
                            });

                });
    }

    private TcpClient buildClient(RsocketConfiguration config) {
        TcpClient client = TcpClient.create()
                .port(config.getPort())
                .host(config.getIp())
                .wiretap(config.isLog());
        try {
            SslContext sslClient = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            return config.isSsl() ? client.secure(sslContextSpec -> sslContextSpec.sslContext(sslClient)) : client;
        } catch (Exception e) {
            config.getThrowableConsumer().accept(e);
            return client;
        }
    }


}
