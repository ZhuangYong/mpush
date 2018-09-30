package com.mpush.common.message.web;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.mpush.api.common.Condition;
import com.mpush.api.connection.Connection;
import com.mpush.api.protocol.Packet;
import com.mpush.api.spi.push.IPushMessage;
import com.mpush.common.condition.AwaysPassCondition;
import com.mpush.common.condition.ScriptCondition;
import com.mpush.common.condition.TagsCondition;
import com.mpush.common.memory.PacketFactory;
import com.mpush.common.message.ByteBufMessage;
import com.mpush.tools.Jsons;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;

import java.util.*;

import static com.mpush.api.protocol.Command.GATEWAY_PUSH;

public final class WebPushMessage extends ByteBufMessage implements IPushMessage {
    public String userId;
    public int clientType;
    public int timeout;
    public byte[] content;

    public String taskId;
    public Set<String> tags;
    public String condition;

    public WebPushMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    public static WebPushMessage build(Connection connection) {
        Packet packet = PacketFactory.get(GATEWAY_PUSH);
        packet.sessionId = genSessionId();
        return new WebPushMessage(packet, connection);
    }

    @Override
    public void decodeJsonBody(Map<String, Object> body) {
        JSONObject jsonBody = (JSONObject) body;
        userId = jsonBody.getString("userId");
        clientType = jsonBody.getInteger("clientType");
        timeout = Optional.ofNullable(jsonBody.getInteger("timeout")).orElse(0);
        content = (jsonBody.getString("content") + "").getBytes();
        taskId = jsonBody.getString("taskId");
        tags = new HashSet<String>(Arrays.asList((jsonBody.getString("tags") + "").split(",")));
        condition = jsonBody.getString("condition");
    }

    @Override
    public void decode(ByteBuf body) {
        userId = decodeString(body);
        clientType = decodeInt(body);
        timeout = decodeInt(body);
        content = decodeBytes(body);
        taskId = decodeString(body);
        tags = decodeSet(body);
        condition = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, userId);
        encodeInt(body, clientType);
        encodeInt(body, timeout);
        encodeBytes(body, content);
        encodeString(body, taskId);
        encodeSet(body, tags);
        encodeString(body, condition);
    }

    private Set<String> decodeSet(ByteBuf body) {
        String json = decodeString(body);
        if (json == null) return null;
        return Jsons.fromJson(json, new TypeReference<Set<String>>() {
        }.getType());
    }

    private void encodeSet(ByteBuf body, Set<String> field) {
        String json = field == null ? null : Jsons.toJson(field);
        encodeString(body, json);
    }

    public WebPushMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public WebPushMessage setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public WebPushMessage setClientType(int clientType) {
        this.clientType = clientType;
        return this;
    }

    public WebPushMessage addFlag(byte flag) {
        packet.addFlag(flag);
        return this;
    }

    public WebPushMessage setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public WebPushMessage setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public WebPushMessage setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public WebPushMessage setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    @Override
    public boolean isBroadcast() {
        return userId == null;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public int getClientType() {
        return clientType;
    }

    @Override
    public int getTimeoutMills() {
        return timeout;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public boolean isNeedAck() {
        return packet.hasFlag(Packet.FLAG_BIZ_ACK) || packet.hasFlag(Packet.FLAG_AUTO_ACK);
    }

    @Override
    public byte getFlags() {
        return packet.flags;
    }

    @Override
    public Condition getCondition() {
        if (condition != null) {
            return new ScriptCondition(condition);
        }
        if (tags != null) {
            return new TagsCondition(tags);
        }
        return AwaysPassCondition.I;
    }


    @Override
    public void finalized() {
        this.content = null;
        this.condition = null;
        this.tags = null;
    }

    @Override
    public void send() {
        super.sendRaw();
    }

    @Override
    public void send(ChannelFutureListener listener) {
        super.sendRaw(listener);
    }

    @Override
    public String toString() {
        return "WebPushMessage{" +
                "userId='" + userId + '\'' +
                ", clientType='" + clientType + '\'' +
                ", timeout='" + timeout + '\'' +
                ", content='" + (content == null ? 0 : content.length) + '\'' +
                '}';
    }
}
