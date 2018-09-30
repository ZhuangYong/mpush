package com.mpush.core.handler;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.message.web.WebPushMessage;
import com.mpush.core.push.PushCenter;

public final class WebSocketPushHandler extends BaseMessageHandler<WebPushMessage> {

    private final PushCenter pushCenter;

    public WebSocketPushHandler(PushCenter pushCenter) {
        this.pushCenter = pushCenter;
    }

    @Override
    public WebPushMessage decode(Packet packet, Connection connection) {
        return new WebPushMessage(packet, connection);
    }

    @Override
    public void handle(WebPushMessage message) {
        Packet packet = message.getPacket();
        GatewayPushMessage msg = new GatewayPushMessage(packet, message.getConnection());
        packet.flags = Packet.FLAG_AUTO_ACK;
        msg.userId = message.userId;
        msg.clientType = message.clientType;
        msg.timeout = message.timeout;
        msg.content = message.content;
        msg.taskId = message.taskId;
        msg.tags = message.tags;
        msg.condition = message.condition;
        pushCenter.push(msg);
    }
}
