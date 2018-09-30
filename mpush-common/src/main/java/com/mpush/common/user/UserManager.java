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

package com.mpush.common.user;

import com.mpush.api.Constants;
import com.mpush.api.router.ClientLocation;
import com.mpush.api.spi.common.CacheManager;
import com.mpush.api.spi.common.CacheManagerFactory;
import com.mpush.api.spi.common.MQClient;
import com.mpush.api.spi.common.MQClientFactory;
import com.mpush.common.CacheKeys;
import com.mpush.common.router.MQKickRemoteMsg;
import com.mpush.common.router.RemoteRouter;
import com.mpush.common.router.RemoteRouterManager;
import com.mpush.tools.config.ConfigTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线列表是存在redis里的，服务被kill -9的时候，无法修改redis。
 * 查询全部在线列表的时候，要通过当前ZK里可用的机器来循环查询。
 * 每台机器的在线列表是分开存的，如果都存储在一起，某台机器挂了，反而不好处理。
 */
public final class UserManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

    private final String onlineUserListKey = CacheKeys.getOnlineUserListKey(ConfigTools.getPublicIp());

    private final String allUserListKey = CacheKeys.getAllUserListKey(ConfigTools.getPublicIp());

    private final CacheManager cacheManager = CacheManagerFactory.create();

    private final MQClient mqClient = MQClientFactory.create();

    private final RemoteRouterManager remoteRouterManager;

    public UserManager(RemoteRouterManager remoteRouterManager) {
        this.remoteRouterManager = remoteRouterManager;
    }

    public void kickUser(String userId) {
        kickUser(userId, -1);
    }

    public void kickUser(String userId, int clientType) {
        Set<RemoteRouter> remoteRouters = remoteRouterManager.lookupAll(userId);
        if (remoteRouters != null) {
            for (RemoteRouter remoteRouter : remoteRouters) {
                ClientLocation location = remoteRouter.getRouteValue();
                if (clientType == -1 || location.getClientType() == clientType) {
                    MQKickRemoteMsg message = new MQKickRemoteMsg()
                            .setUserId(userId)
                            .setClientType(location.getClientType())
                            .setConnId(location.getConnId())
                            .setDeviceId(location.getDeviceId())
                            .setTargetServer(location.getHost())
                            .setTargetPort(location.getPort());
                    mqClient.publish(Constants.getKickChannel(location.getHostAndPort()), message);
                }
            }
        }
    }

    public void clearOnlineUserList() {
        cacheManager.del(onlineUserListKey);
    }

    public void addToOnlineList(String userId, int clientType) {
        cacheManager.zAdd(onlineUserListKey, userId);
        cacheManager.zAdd(onlineUserListKey + ":" + clientType, userId);

        cacheManager.zAdd(allUserListKey, userId);
        cacheManager.zAdd(allUserListKey + ":" + clientType, userId);
        LOGGER.info("user online {}", userId);
    }

    public void remFormOnlineList(String userId, int clientType) {
        cacheManager.zRem(onlineUserListKey, userId);
        cacheManager.zRem(onlineUserListKey + ":" + clientType, userId);
        LOGGER.info("user offline {}", userId);
    }

    // 跟踪用户
    public void trackUser(String myId, String userId) {
        String trackUserKey = CacheKeys.trackUserKey(userId);
        cacheManager.zAdd(trackUserKey , myId);
        LOGGER.info("user in track myId={} userId={}", myId, userId);
    }

    // 取消跟踪用户
    public void unTrackUser(String userId) {
        String trackUserKey = CacheKeys.trackUserKey(userId);
        cacheManager.del(trackUserKey);
        LOGGER.info("user not in track userId={}", userId);
    }

    // 取消跟踪用户
    public void unTrackUser(String userId, String myId) {
        String trackUserKey = CacheKeys.trackUserKey(userId);
        cacheManager.zRem(trackUserKey, myId);
        LOGGER.info("user not in my track myId={} userId={}", myId, userId);
    }

    // 用户被跟踪人数
    public long userInTrackSize(String userId) {
        String trackUserKey = CacheKeys.trackUserKey(userId);
        return Optional.ofNullable(cacheManager.zCard(trackUserKey)).orElse(0L);
    }

    // 用户被跟踪列表
    public List<String> userTrackList(String userId, int start, int end) {
        String trackUserKey = CacheKeys.trackUserKey(userId);
        return cacheManager.zrange(trackUserKey, start, end, String.class);
    }

    //在线用户数量
    public long getOnlineUserNum() {
        Long value = cacheManager.zCard(onlineUserListKey);
        return value == null ? 0 : value;
    }

    //所有用户数量
    public long getAllUserNum() {
        Long value = cacheManager.zCard(allUserListKey);
        return value == null ? 0 : value;
    }

    //在线用户数量
    public long getOnlineUserNum(int clientType) {
        Long value = cacheManager.zCard(onlineUserListKey + ":" + clientType);
        return value == null ? 0 : value;
    }

    // 所有用户数量
    public long getAllUserNum(int clientType) {
        Long value = cacheManager.zCard(allUserListKey + ":" + clientType);
        return value == null ? 0 : value;
    }

    //在线用户数量
    public long getOnlineUserNum(String publicIP) {
        String online_key = CacheKeys.getOnlineUserListKey(publicIP);
        Long value = cacheManager.zCard(online_key);
        return value == null ? 0 : value;
    }

    // 所有用户数量
    public long getAllUserNum(String publicIP) {
        String online_key = CacheKeys.getAllUserListKey(publicIP);
        Long value = cacheManager.zCard(online_key);
        return value == null ? 0 : value;
    }

    //在线用户数量
    public long getOnlineUserNum(String publicIP, int clientType) {
        String online_key = CacheKeys.getOnlineUserListKey(publicIP + ":" + clientType);
        Long value = cacheManager.zCard(online_key);
        return value == null ? 0 : value;
    }

    // 所有用户数量
    public long getAllUserNum(String publicIP, int clientType) {
        String online_key = CacheKeys.getAllUserListKey(publicIP + ":" + clientType);
        Long value = cacheManager.zCard(online_key);
        return value == null ? 0 : value;
    }

    //在线用户列表
    public List<String> getOnlineUserList(String publicIP, int start, int end) {
        String key = CacheKeys.getOnlineUserListKey(publicIP);
        return cacheManager.zrange(key, start, end, String.class);
    }
    // 所有用户列表
    public List<String> getAllUserList(String publicIP, int start, int end) {
        String key = CacheKeys.getAllUserListKey(publicIP);
        return cacheManager.zrange(key, start, end, String.class);
    }

    //在线用户列表
    public List<String> getOnlineUserList(String publicIP, int clientType, int start, int end) {
        String key = CacheKeys.getOnlineUserListKey(publicIP + ":" + clientType);
        return cacheManager.zrange(key, start, end, String.class);
    }

    //在线用户列表
    public List<String> getAllUserList(String publicIP, int clientType, int start, int end) {
        String key = CacheKeys.getAllUserListKey(publicIP + ":" + clientType);
        return cacheManager.zrange(key, start, end, String.class);
    }

    // 用户
    public Set<RemoteRouter> lookupAll(String userId) {
        String key = CacheKeys.getUserRouteKey(userId);
        Map<String, ClientLocation> values = cacheManager.hgetAll(key, ClientLocation.class);
        if (values == null || values.isEmpty()) return Collections.emptySet();
        return values.values().stream().map(RemoteRouter::new).collect(Collectors.toSet());
    }

    public RemoteRouter lookup(String userId, int clientType) {
        String key = CacheKeys.getUserRouteKey(userId);
        String field = Integer.toString(clientType);
        ClientLocation location = cacheManager.hget(key, field, ClientLocation.class);
        return location == null ? null : new RemoteRouter(location);
    }
}
