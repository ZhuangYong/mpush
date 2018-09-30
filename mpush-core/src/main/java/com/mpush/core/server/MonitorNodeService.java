package com.mpush.core.server;

import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.Subscribe;
import com.mpush.api.event.TrackUserMessageEvent;
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
        String userId = event.getUserId();
        if (userId == null) {
            userId = message.getConnection().getSessionContext().userId;
        }
        String trackUserKey = CacheKeys.trackUserKey(userId);
        List<String> monitorUsers = cacheManager.zrange(trackUserKey, 0, 100, String.class);
        // 在跟踪列表
        if (monitorUsers != null) {
            JSONObject content = new JSONObject();
            content.put("message", message.getPacket().toString().getBytes());
            content.put("sessionId", event.getSessionId());
            content.put("userId", event.getUserId());
            content.put("time", event.getTime());
            content.put("desc", event.getDesc());

            for (String monitorUserId: monitorUsers) {
                Set<RemoteRouter> monitor = lookupAll(monitorUserId);
                if (monitor != null) {
                    monitor.forEach(m -> {
                        ClientLocation clientLocation = m.getRouteValue();
                        Packet packet = message.getPacket();
                        packet.sessionId = -message.getSessionId();
                        GatewayPushMessage msg = new GatewayPushMessage(packet, message.getConnection());
                        packet.flags = 0;
                        msg.userId = monitorUserId;
                        msg.clientType = clientLocation.getClientType();
                        msg.timeout = 3000;
                        msg.content = content.toString().getBytes();
                        mPushServer.getPushCenter().push(msg);
                    });
                }
            }
        }
    }
}
