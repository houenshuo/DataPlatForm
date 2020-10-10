package iot.ccnu.dataCollect.mqttServer.transport.server;

import iot.ccnu.dataCollect.mqttServer.api.server.RsocketServerSession;
import iot.ccnu.dataCollect.mqttServer.api.server.handler.RsocketMessageHandler;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.config.RsocketServerConfig;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 消息代理 服务端
 */
public class TransportServer {
    private static RsocketServerConfig config;
    private static TransportServerFactory transportFactory;


    private  TransportServer(){
    }
    //构建配置属性
    public  static class TransportBuilder{

        public TransportBuilder(){
            config = new RsocketServerConfig();
            transportFactory = new TransportServerFactory();
        }

        public TransportBuilder(String ip,int port){
            this();
            config.setIp(ip);
            config.setPort(port);
        }

        public TransportBuilder protocol(ProtocolType protocolType){
            config.setProtocol(protocolType.name());
            return this;
        }

        public TransportBuilder heart(int  heart){
            config.setHeart(heart);
            return this;
        }

        public TransportBuilder ssl(boolean  ssl){
            config.setSsl(ssl);
            return this;
        }

        public TransportBuilder log(boolean  log){
            config.setLog(log);
            return this;
        }

        public TransportBuilder keepAlive(boolean  isKeepAlive){
            config.setKeepAlive(isKeepAlive);
            return this;
        }
        public TransportBuilder noDelay(boolean  noDelay){
            config.setNoDelay(noDelay);
            return this;
        }

        public TransportBuilder backlog(int  length){
            config.setBacklog(length);
            return this;
        }
        public TransportBuilder sendBufSize(int  size){
            config.setSendBufSize(size);
            return this;
        }

        public TransportBuilder revBufSize(int  size){
            config.setRevBufSize(size);
            return this;
        }


        public TransportBuilder auth(BiFunction<String,String,Boolean> auth){
            config.setAuth(auth);
            return this;
        }


        public TransportBuilder messageHandler(RsocketMessageHandler messageHandler ){
            Optional.ofNullable(messageHandler)
                    .ifPresent(config::setMessageHandler);
            return this;
        }

        public TransportBuilder exception(Consumer<Throwable> exceptionConsumer ){
            Optional.ofNullable(exceptionConsumer)
                    .ifPresent(config::setThrowableConsumer);
            return this;
        }


        public Mono<RsocketServerSession> start(){
            config.checkConfig();
            return transportFactory.start(config);
        }
    }

    public static TransportBuilder create(String ip, int port){
        return  new TransportBuilder(ip,port);
    }

    public static  TransportBuilder create(){
        return  new TransportBuilder();
    }
}

