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

package com.mpush.client.push;

import com.google.common.collect.Maps;
import com.mpush.api.push.PushSender;
import com.mpush.api.connection.Connection;
import com.mpush.api.router.ClientLocation;
import com.mpush.common.message.gateway.GatewayPushMessage;
import com.mpush.common.router.ConnectionRouterManager;
import com.mpush.common.router.RemoteRouter;
import com.mpush.tools.Jsons;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ohun on 2015/12/30.
 *
 * @author ohun@live.cn
 */
public class PushRequest implements PushSender.Callback, Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushRequest.class);

    private final PushClient client;

    private PushSender.Callback callback;
    private String userId;
    private String content;
    private long timeout;
    private long timeout_;
    private int sessionId;
    private long sendTime;
    private AtomicInteger status = new AtomicInteger(0);
    private Map<String, Long> times = Maps.newHashMap();

    public PushRequest(PushClient client) {
        this.client = client;
    }

    public static PushRequest build(PushClient client) {
        return new PushRequest(client);
    }

    public PushRequest setCallback(PushSender.Callback callback) {
        this.callback = callback;
        return this;
    }

    public PushRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public PushRequest setContent(String content) {
        this.content = content;
        return this;
    }

    public PushRequest setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public void onSuccess(String userId) {
        putTime("success");
        LOGGER.info("success,sessionId:{},times:{},content:{}", sessionId, Jsons.toJson(times), content);
        submit(1);
    }

    @Override
    public void onFailure(String userId) {
        putTime("failure");
        LOGGER.info("failure,sessionId:{},times:{},content:{}", sessionId, Jsons.toJson(times), content);
        submit(2);
    }

    @Override
    public void onOffline(String userId) {
        putTime("offline");
        LOGGER.info("offline,sessionId:{},times:{},content:{}", sessionId, Jsons.toJson(times), content);
        submit(3);
    }

    @Override
    public void onTimeout(String userId) {
        putTime("timeout");
        LOGGER.info("timeout,sessionId:{},times:{},content:{}", sessionId, Jsons.toJson(times), content);
        submit(4);
    }

    private void submit(int status) {
        if (this.status.compareAndSet(0, status)) {//防止重复调用
            if (callback != null) {
                PushRequestBus.INSTANCE.getExecutor().execute(this);
            } else {
                LOGGER.warn("callback is null");
            }
        }
    }

    @Override
    public void run() {
        switch (status.get()) {
            case 1:
                callback.onSuccess(userId);
                break;
            case 2:
                callback.onFailure(userId);
                break;
            case 3:
                callback.onOffline(userId);
                break;
            case 4:
                callback.onTimeout(userId);
                break;
        }
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() > timeout_;
    }

    public void timeout() {
        onTimeout(userId);
    }

    public void success() {
        onSuccess(userId);
    }

    public void failure() {
        onFailure(userId);
    }

    public void offline() {
        ConnectionRouterManager.INSTANCE.invalidateLocalCache(userId);
        onOffline(userId);
    }

    public void send() {
        this.timeout_ = timeout + System.currentTimeMillis();
        putTime("startsend");
        sendToConnServer();
    }

    public void redirect() {
        ConnectionRouterManager.INSTANCE.invalidateLocalCache(userId);
        LOGGER.warn("user route has changed, userId={}, content={}", userId, content);
        if (status.get() == 0) {
            send();
        }
    }

    private void sendToConnServer() {
        //1.查询用户长连接所在的机器
        RemoteRouter router = ConnectionRouterManager.INSTANCE.lookup(userId);
        if (router == null) {
            //1.1没有查到说明用户已经下线
            this.onOffline(userId);
            return;
        }

        //2.通过网关连接，把消息发送到所在机器
        ClientLocation location = router.getRouteValue();
        Connection gatewayConn = client.getGatewayConnection(location.getHost());
        if (gatewayConn == null || !gatewayConn.isConnected()) {
            this.onFailure(userId);
            return;
        }

        putTime("sendtoconnserver");

        final GatewayPushMessage pushMessage = new GatewayPushMessage(userId, content, gatewayConn);
        pushMessage.sendRaw(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    putTime("sendsuccess");
                } else {
                    PushRequest.this.onFailure(userId);
                }
            }
        });

        sessionId = pushMessage.getSessionId();
        putTime("putrequestbus");
        PushRequestBus.INSTANCE.put(sessionId, this);
    }

    public long getSendTime() {
        return sendTime;
    }

    public Map<String, Long> getTimes() {
        return times;
    }

    public void putTime(String key) {
        this.times.put(key, System.currentTimeMillis());
    }

}