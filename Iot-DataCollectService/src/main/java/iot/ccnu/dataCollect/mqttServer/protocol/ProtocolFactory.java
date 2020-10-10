package iot.ccnu.dataCollect.mqttServer.protocol;

import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.protocol.mqtt.MqttProtocol;
import iot.ccnu.dataCollect.mqttServer.protocol.ws.WsProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 协议工厂
 *      根据类型mqtt 、 websocket的 server 和 client
 */
public class ProtocolFactory {

    private List<Protocol> protocols = new ArrayList<>();

    public ProtocolFactory(){

        protocols.add(new MqttProtocol());
        protocols.add(new WsProtocol());
    }


    public void  registryProtocl(Protocol protocol){
        protocols.add(protocol);
    }

    public Optional<Protocol> getProtocol(ProtocolType protocolType){
        return protocols.stream().filter(protocol -> protocol.support(protocolType)).findAny();
    }




}
