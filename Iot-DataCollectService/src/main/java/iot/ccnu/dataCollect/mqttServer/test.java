package iot.ccnu.dataCollect.mqttServer;

import iot.ccnu.dataCollect.mqttServer.api.client.RsocketClientSession;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.transport.client.TransportClient;

public class test {
    public static void main(String[] args) throws InterruptedException {
        RsocketClientSession clientSession= TransportClient.create("localhost",1883)
                .heart(10000)
                .protocol(ProtocolType.MQTT)
                .ssl(false)
                .log(false)
                .clientId("consumer")
                .password("12")
                .username("123")
                .willMessage("123")
                .willTopic("/lose")
                .exception(throwable -> System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+throwable))
                .messageAcceptor((topic,msg)->{
                    System.out.println(topic+":"+new String(msg));
                })
                .connect()//连接消息代理服务器
                .block();
        Thread.sleep(5000);
        clientSession.sub("sensorData").subscribe();
    }
}
