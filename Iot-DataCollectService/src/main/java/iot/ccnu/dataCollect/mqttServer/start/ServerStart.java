package iot.ccnu.dataCollect.mqttServer.start;

import iot.ccnu.dataCollect.mqttServer.api.TransportConnection;
import iot.ccnu.dataCollect.mqttServer.api.server.RsocketServerSession;
import iot.ccnu.dataCollect.mqttServer.api.server.handler.MemoryMessageHandler;
import iot.ccnu.dataCollect.mqttServer.common.annocation.ProtocolType;
import iot.ccnu.dataCollect.mqttServer.transport.server.TransportServer;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ServerStart {
    //启动消息代理server
    public void start() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        RsocketServerSession serverSession= TransportServer.create("localhost",1883)
                .auth((s,p)->true)
                .heart(100000)
                .protocol(ProtocolType.MQTT)
                .ssl(false)
                .auth((username,password)->true)
                .log(false)
                .messageHandler(new MemoryMessageHandler())
                .exception(throwable -> System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+throwable))
                .start()//启动消息代理服务
                .block();
        serverSession.closeConnect("device-1").subscribe();// 关闭设备端
        List<TransportConnection> connections= serverSession.getConnections().block(); // 获取所有链接
        latch.await();
    }
}
