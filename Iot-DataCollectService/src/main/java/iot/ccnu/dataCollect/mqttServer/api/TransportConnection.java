package iot.ccnu.dataCollect.mqttServer.api;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import iot.ccnu.dataCollect.mqttServer.common.message.TransportMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.LongAdder;

@Getter
@Setter
@ToString
@Slf4j
/**
 * mqtt报文传输
 *      1，基于reactor-netty中的Connection,并结合mqtt相关协议报文，实现相关报文的发送（依靠netty实现消息发送），协议报文有14种（netty有相关mqtt报文定义）
 *          mqtt是双工通信，所以报文的流动是双向的，client使用TransportConnection，server获取对应的TransportConnection
 *      2，RsocketClientConnection和RsocketServerConnection会在进一步封装为具体的server和client所使用
 *
 */
public class TransportConnection implements Disposable {
    //i/o 读数据：client读取server发来的报文 或者 server读取client发来的mqtt协议报文
    private NettyInbound inbound;
    //I/o的handler有用户写数据的能力  对应Netty中的outbound：用户发起的，如 链接操作、绑定操作、消息发送
    // server个client使用outbound来进行报文的发送
    private NettyOutbound outbound;
    //
    private Connection connection;
    //计数器
    private LongAdder longAdder = new LongAdder();

    private ConcurrentHashMap<Integer,Disposable> concurrentHashMap = new ConcurrentHashMap<>();//

    private ConcurrentHashMap<Integer, TransportMessage> qos2Message = new ConcurrentHashMap<>();//服务质量等级3：“仅仅一次”

    private List<String> topics   = new CopyOnWriteArrayList<>();// 主题 集合


    public <T> Flux<T> receive(Class<T> tClass){
        return  inbound.receive().cast(tClass);
    }

    public  TransportConnection(Connection connection){
        this.connection=connection;
        this.inbound=connection.inbound();
        this.outbound=connection.outbound();
    }


    public void destory() {
        concurrentHashMap.values().forEach(Disposable::dispose);
        concurrentHashMap.clear();
        qos2Message.clear();
        topics.clear();
        // 所有设备下线

    }


    public void addTopic(String topic){
        topics.add(topic);
    }

    public void removeTopic(String topic){
        topics.remove(topic);
    }




    /**
     * 控制报文的发送：心跳
     * @return
     */
    public Mono<Void> sendPingReq(){
        Mono<Void> mono =null;
        try {
            mono = outbound
                    .sendObject(new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0)))
                    .then();
        }catch (Exception e){
            e.printStackTrace();
        }
        return mono;
    }
    public Mono<Void> sendPingRes(){
        Mono<Void> mono =null;
        try {
            mono = outbound
                    .sendObject(new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0)))
                    .then();
        }catch (Exception e){
            e.printStackTrace();
        }
        return mono;
    }
    /**
     * 控制报文发送:发送消息
     * @return
     */
    public  Mono<Void> sendMessage(boolean isDup, MqttQoS qoS, boolean isRetain, String topic, byte[] message){
        //构建控制报文
        return this.write(MqttMessageApi.buildPub(isDup,qoS,isRetain,1,topic, Unpooled.wrappedBuffer(message)));
    }

    public   Mono<Void> sendMessageRetry(boolean isDup, MqttQoS qoS, boolean isRetain, String topic, byte[] message){
        int id = this.messageId();
        //设置重发操作（根据不同的服务质量等级）
        this.addDisposable(id, Mono
                .fromRunnable(() ->this.write(MqttMessageApi.buildPub(isDup,qoS,isRetain,id,topic, Unpooled.wrappedBuffer(message))).subscribe())
                .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
        //构建发送消息类型的 控制报文
        MqttPublishMessage publishMessage = MqttMessageApi.buildPub(isDup,qoS,isRetain,id,topic, Unpooled.wrappedBuffer(message));
        return this.write(publishMessage);
    }
    //最终使用netty进行消息发送
    public Mono<Void> write(Object object){
        Mono<Void> mono =null;
        try {
            log.info("write:"+object);
            mono = outbound.sendObject(object).then().doOnError((message)->{
                System.out.println("客户端出异常了");
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return mono;
    }



    /**
     * 消息 自增id
     * @return
     */
    public int messageId(){
        longAdder.increment();
        int value=longAdder.intValue();
        if(value==Integer.MAX_VALUE){
            longAdder.reset();
            longAdder.increment();
            return longAdder.intValue();
        }
        return value;
    }


    /**
     * 服务质量等级3 消息的map管理
     * @param messageId
     * @param message
     */
    public  void  saveQos2Message(Integer messageId,TransportMessage message){
        qos2Message.put(messageId,message);
    }
    public Optional<TransportMessage> getAndRemoveQos2Message(Integer messageId){
        TransportMessage message  = qos2Message.get(messageId);
        qos2Message.remove(messageId);
        return Optional.ofNullable(message);
    }
    public  boolean  containQos2Message(Integer messageId,byte[] bytes){
        return qos2Message.containsKey(messageId);
    }


    /**
     * 资源的 map管理
     * @param messageId
     * @param disposable
     */
    public  void  addDisposable(Integer messageId,Disposable disposable){
        concurrentHashMap.put(messageId,disposable);
    }
    public  void  cancelDisposable(Integer messageId){
        Optional.ofNullable(concurrentHashMap.get(messageId))
                .ifPresent(dispose->dispose.dispose());
        concurrentHashMap.remove(messageId);
    }



    @Override
    public void dispose() {
        connection.dispose();
    }

    public boolean isDispose(){
        return connection.isDisposed();
    }

}
