package iot.ccnu.dataCollect.mqttServer.api;

import io.netty.util.AttributeKey;
import iot.ccnu.dataCollect.mqttServer.api.client.RsocketClientSession;
import iot.ccnu.dataCollect.mqttServer.api.server.RsocketServerSession;
import iot.ccnu.dataCollect.mqttServer.common.connection.WillMessage;
import lombok.experimental.UtilityClass;
import reactor.core.Disposable;
@UtilityClass
public class AttributeKeys {

    public AttributeKey<RsocketClientSession> clientConnectionAttributeKey = AttributeKey.valueOf("client_operation");

    public AttributeKey<RsocketServerSession> serverConnectionAttributeKey = AttributeKey.valueOf("server_operation");

    public AttributeKey<Disposable> closeConnection = AttributeKey.valueOf("close_connection");

    public AttributeKey<TransportConnection> connectionAttributeKey = AttributeKey.valueOf("transport_connection");

    public AttributeKey<String> device_id = AttributeKey.valueOf("device_id");

    public AttributeKey<Integer> keepalived = AttributeKey.valueOf("keepalived");

    public AttributeKey<WillMessage> WILL_MESSAGE = AttributeKey.valueOf("WILL_MESSAGE");

}
