package iot.ccnu.dataCollect.mqttServer.transport.server.handler;

import io.netty.handler.codec.mqtt.MqttMessage;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.config.RsocketServerConfig;
import iot.ccnu.dataCollect.mqttServer.transport.DirectHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
/**
 * server读取消息后的  转发中心
 */
public class ServerMessageRouter {


    private final RsocketServerConfig config;
    //消息适配
    private final DirectHandlerAdaptor directHandlerAdaptor;

    public ServerMessageRouter(RsocketServerConfig config) {
        this.config=config;
        this.directHandlerAdaptor= DirectHandlerFactory::new;//实现handler方法 返回DirectHandlerFactory对象
    }

    public void handler(MqttMessage message, TransportConnection connection) {
        if(message.decoderResult().isSuccess()){
            log.info("服务端处理控制报文 {} info{}",connection.getConnection(),message);
            DirectHandler handler=directHandlerAdaptor.handler(message.fixedHeader().messageType()).loadHandler();
            handler.handler(message,connection,config);//转发消息
        }
        else {
            log.error("accept message  error{}",message.decoderResult().toString());
        }
    }

}

