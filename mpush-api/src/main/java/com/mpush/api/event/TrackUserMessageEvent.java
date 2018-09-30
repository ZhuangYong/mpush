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

package com.mpush.api.event;

import io.netty.handler.codec.json.JsonObjectDecoder;

public final class TrackUserMessageEvent implements Event {

    private final Object message;
    private final String sessionId;
    private final String userId;
    private final long time;
    private final String desc;


    public TrackUserMessageEvent(String userId, Object message, String desc) {
        this.message = message;
        this.sessionId = "";
        this.userId = userId;
        this.time = System.currentTimeMillis();
        this.desc = desc;
    }

    public TrackUserMessageEvent(String userId, String sessionId, String desc) {
        this.message = null;
        this.sessionId = sessionId;
        this.userId = userId;
        this.time = System.currentTimeMillis();
        this.desc = desc;
    }

    public Object getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }

    public String getDesc() {
        return desc;
    }

    public long getTime() {
        return time;
    }

    public String getSessionId() {
        return sessionId;
    }
}
