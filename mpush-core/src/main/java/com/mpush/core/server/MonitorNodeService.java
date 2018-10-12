package com.mpush.core.server;

import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.Subscribe;
import com.mpush.api.Constants;
import com.mpush.api.event.TrackUserMessageEvent;
import com.mpush.api.protocol.Command;
import com.mpush.api.protocol.JsonPacket;
import com.mpush.api.protocol.Packet;
import com.mpush.api.router.ClientLocation;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.api.spi.common.CacheManagerFactory;
import com.mpush.common.CacheKeys;
import com.mpush.common.message.BaseMessage;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.router.RemoteRouter;
import com.mpush.core.MPushServer;
import com.mpush.tools.event.EventBus;

import java.util.List;
import java.util.Set;

import static com.mpush.tools.config.CC.mp.net.tcpGateway;

public class MonitorNodeService {

    private MPushServer mPushServer;

    private static final CacheManager cacheManager = CacheManagerFactory.create();

    public MonitorNodeService(MPushServer mPushServer) {
        this.mPushServer = mPushServer;
        EventBus.register(this);
    }

    @Subscribe
    void on(TrackUserMessageEvent event) {
        BaseMessage message = (BaseMessage)event.getMessage();
        trackSendMessage(event, message);

    }

    /**
     * 查找错油用户
     * @param userId
     * @return
     */
    public Set<RemoteRouter> lookupAll(String userId) {
        return mPushServer.getRouterCenter().getRemoteRouterManager().lookupAll(userId);
    }

    /**
     * 最多只支持100个
     * @param event
     */
    private void trackSendMessage(TrackUserMessageEvent event, BaseMessage message) {
        if (message.getSessionId() < 0) {
            return;
        }
        String userId = event.getUserId();
        String formUserId = message.getConnection().getSessionContext().userId;
        String trackUserKey = CacheKeys.trackUserKey(userId);
        List<String> monitorUsers = cacheManager.zrange(trackUserKey, 0, 100, String.class);
        if (monitorUsers == null || monitorUsers.isEmpty()) {
            trackUserKey = CacheKeys.trackUserKey(formUserId);
            monitorUsers = cacheManager.zrange(trackUserKey, 0, 100, String.class);
        }
        // 在跟踪列表
        if (monitorUsers != null) {
            JSONObject content = new JSONObject();
            content.put("message", JSONObject.toJSON(message.getPacket()).toString());
            content.put("sessionId", -message.getSessionId());
            content.put("hash", message.getHash());
            content.put("formUserId", formUserId);
            content.put("userId", userId);
            content.put("time", event.getTime());
            content.put("desc", event.getDesc());

            for (String monitorUserId: monitorUsers) {
                Set<RemoteRouter> monitor = lookupAll(monitorUserId);
                if (monitor != null) {
                    monitor.forEach(m -> {
                        ClientLocation clientLocation = m.getRouteValue();
                        Packet packet = message.getPacket();
                        JsonPacket jsonPacket = new JsonPacket(Command.toCMD(packet.cmd), -message.getSessionId());
                        jsonPacket.setBody(packet.body);
                        GatewayPushMessage msg = new GatewayPushMessage(jsonPacket, message.getConnection());
                        packet.flags = Packet.FLAG_AUTO_ACK;
                        msg.userId = monitorUserId;
                        msg.clientType = clientLocation.getClientType();
                        msg.timeout = 3000;
                        msg.content = content.toString().getBytes(Constants.UTF_8);
                        mPushServer.getPushCenter().push(msg);
                    });
                }
            }
        }
    }
}
