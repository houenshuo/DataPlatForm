package iot.ccnu.dataCollect.mqttServer;


import iot.ccnu.dataCollect.mqttServer.api.client.RsocketClientSession;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.transport.client.TransportClient;


public class Producer_3 {

    public static void main(String[] args) throws InterruptedException {

        RsocketClientSession clientSession= TransportClient.create("localhost",1883)
                .heart(5000)
                .protocol(ProtocolType.MQTT)
                .onClose(()->{
                    System.out.println("客户端关闭了");
                })
                .ssl(false)
                .log(false)
                .clientId("Produce1")
                .password("12")
                .username("123")
                .willMessage("123")
                .willTopic("/lose")
                .exception(throwable -> System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+throwable))
                .messageAcceptor((topic,msg)->{
                    System.out.println(topic+":"+new String(msg));
                })
                .connect()
                .block();
        for(int i=0;i<20000000;i++){
            clientSession.pub("consumer","consumer11".getBytes(),0).subscribe();
            Thread.sleep(1000);
        }

    }

}
