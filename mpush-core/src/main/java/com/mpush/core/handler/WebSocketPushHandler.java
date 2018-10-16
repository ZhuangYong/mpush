package com.mpush.core.handler;

import com.mpush.api.Constants;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.api.push.*;
import com.mpush.common.handler.BaseMessageHandler;
import com.mpush.common.message.ErrorMessage;
import com.mpush.common.message.OkMessage;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.message.web.WebPushMessage;
import com.mpush.core.MPushServer;
import com.mpush.core.push.PushCenter;
import com.mpush.core.push.PushTask;
import com.mpush.core.router.LocalRouter;
import com.mpush.tools.common.TimeLine;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.mpush.common.push.GatewayPushResult.toJson;

public final class WebSocketPushHandler extends BaseMessageHandler<WebPushMessage> {
    private final PushSender sender = PushSender.create();
    private final PushCenter pushCenter;
    private final MPushServer mPushServer;

    public WebSocketPushHandler(MPushServer mPushServer) {
        this.pushCenter = mPushServer.getPushCenter();
        this.mPushServer = mPushServer;
        sender.start().join();
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

        Set<LocalRouter> localRouter = mPushServer.getRouterCenter().getLocalRouterManager().lookupAll(message.userId);
        if (localRouter.size() == 0) {
            TimeLine timeLine = new TimeLine();
            timeLine.begin("push-redirect-begin");
            PushMsg redirectMsg = PushMsg.build(MsgType.MESSAGE,  new String(message.content, Constants.UTF_8));
            PushContext context = PushContext.build(redirectMsg)
                    .setAckModel(AckModel.AUTO_ACK)
                    .setUserId(msg.userId)
                    .setBroadcast(false)
                    .setTimeout(message.timeout)
                    .setCallback(new PushCallback() {
                        @Override
                        public void onResult(PushResult result) {
                            timeLine.addTimePoint("push-redirect-send");
                            pushCenter.addTask(new PushTask() {
                                @Override
                                public ScheduledExecutorService getExecutor() {
                                    return message.getExecutor();
                                }

                                @Override
                                public void run() {
                                    int resultCode = result.getResultCode();
                                    if (resultCode == PushResult.CODE_SUCCESS) {
                                        OkMessage
                                                .from(message)
                                                .setData(toJson(msg, timeLine.getTimePoints()))
                                                .sendRaw();
                                    } else {
                                        ErrorMessage.from(message).setReason(result.getResultDesc()).close();
                                    }

                                }
                            });
                        }
                    });
            sender.send(context);
        } else {
            pushCenter.push(msg);
        }
    }
}
