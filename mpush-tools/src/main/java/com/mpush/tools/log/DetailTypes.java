package com.mpush.tools.log;

/**
 * 日志详细类型
 */
public enum DetailTypes {
    // single user push message
    SINGLE_USER_PUSH_MESSAGE(0),
    BROADCAST_PUSH_MESSAGE(1),
    GATEWAY_PUSH_MESSAGE(2),
    CLIENT_PUSH_MESSAGE(3),
    WEB_SOCKET_PUSH_MESSAGE(4),
    PUSH_MESSAGE_ACK(5),
    HANDSHAKE(6),
    BIND_USER(7),
    UNBIND_USER(8),
    FAST_CONN(9),
    HTTP_PROXY(10),
    ;

    private Integer value;

    DetailTypes(int i) {
        this.value = i;
    }

    public Integer getValue() {
        return value;
    }
}
