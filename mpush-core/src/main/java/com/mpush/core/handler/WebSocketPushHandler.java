/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.mpush.core.handler;

import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.ByteBufMessage;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.message.web.WebPushMessage;
import com.mpush.core.push.PushCenter;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
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
