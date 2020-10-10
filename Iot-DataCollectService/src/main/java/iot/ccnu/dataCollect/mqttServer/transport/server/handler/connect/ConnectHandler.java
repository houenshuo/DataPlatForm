package iot.ccnu.dataCollect.mqttServer.transport.server.handler.connect;

import io.netty.handler.codec.mqtt.*;
import io.netty.util.Attribute;
import iot.ccnu.dataCollect.mqttServer.api.AttributeKeys;
import iot.ccnu.dataCollect.mqttServer.api.MqttMessageApi;
import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.config.RsocketConfiguration;
import iot.ccnu.dataCollect.mqttServer.api.server.handler.RsocketChannelManager;
import iot.ccnu.dataCollect.mqttServer.common.connection.WillMessage;
import iot.ccnu.dataCollect.mqttServer.config.RsocketServerConfig;
import iot.ccnu.dataCollect.mqttServer.transport.DirectHandler;
import reactor.core.Disposable;

import java.util.Optional;

/**
 * 客户端 连接-断开 连接
 */
public class ConnectHandler implements DirectHandler {


    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        RsocketServerConfig serverConfig = (RsocketServerConfig) config;
        switch (message.fixedHeader().messageType()){
            case CONNECT:
                System.out.println("服务端接受到了客户端的连接请求--------------");
                MqttConnectMessage connectMessage =(MqttConnectMessage) message;
                MqttConnectVariableHeader connectVariableHeader=connectMessage.variableHeader();
                MqttConnectPayload mqttConnectPayload=connectMessage.payload();
                RsocketChannelManager channelManager=serverConfig.getChannelManager();
                String clientId=mqttConnectPayload.clientIdentifier();
                channelManager.checkDeviceId(clientId).subscribe(m->{
                    System.out.println("设备校验--------------"+m);
                    if (!m){
                        // 通知设备校验失败
                        connection.write( MqttMessageApi.buildConnectAck(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED)).subscribe();
                        connection.dispose();
                    }
                });
                if(connectVariableHeader.hasPassword() && connectVariableHeader.hasUserName()){
                    if(serverConfig.getAuth().apply(mqttConnectPayload.userName(),mqttConnectPayload.password()))
                        connectSuccess(connection,serverConfig.getChannelManager(),clientId,connectVariableHeader.keepAliveTimeSeconds());
                    else connection.write( MqttMessageApi.buildConnectAck(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD)).subscribe();
                    if(connectVariableHeader.isWillFlag())
                        saveWill(connection,mqttConnectPayload.willTopic(),connectVariableHeader.isWillRetain(),mqttConnectPayload.willMessageInBytes(),connectVariableHeader.willQos());
                }
                else {
                    connectSuccess(connection,channelManager,mqttConnectPayload.clientIdentifier(),connectVariableHeader.keepAliveTimeSeconds());
                    if(connectVariableHeader.isWillFlag())
                        saveWill(connection,mqttConnectPayload.willTopic(),connectVariableHeader.isWillRetain(),mqttConnectPayload.willMessageInBytes(),connectVariableHeader.willQos());
                }
                break;
            case DISCONNECT:
                serverConfig.getChannelManager().removeConnections(connection);
                connection.dispose();
                break;
        }
    }

    private void   saveWill(TransportConnection connection,String willTopic,boolean willRetain ,byte[] willMessage,int qoS){
        WillMessage ws =  new WillMessage(qoS,willTopic,willMessage,willRetain);
        connection.getConnection().channel().attr(AttributeKeys.WILL_MESSAGE).set(ws); // 设置device Id
    }

    private  void  connectSuccess(TransportConnection connection, RsocketChannelManager channelManager, String deviceId, int keepalived){
        connection.getConnection().onReadIdle(keepalived*2, () -> {// 设置connection的读心跳 keepalived客户端传来的
            System.out.println("客户端心跳没有收到，出现异常---------------");
        });
        connection.getConnection().channel().attr(AttributeKeys.device_id).set(deviceId); // 设置device Id

        channelManager.addDeviceId(deviceId,connection);
        Optional.ofNullable(connection.getConnection().channel().attr(AttributeKeys.closeConnection)) // 取消关闭连接
                .map(Attribute::get)
                .ifPresent(Disposable::dispose);
        channelManager.addConnections(connection);
        // 通知设备上线  访问设备模块

        connection.write( MqttMessageApi.buildConnectAck(MqttConnectReturnCode.CONNECTION_ACCEPTED)).subscribe();//连接回复信号
    }
}
