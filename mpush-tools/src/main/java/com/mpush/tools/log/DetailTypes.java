package com.mpush.tools.log;

/**
 * 日志详细类型
 */
public enum DetailTypes {
    // receive client push message
    RECEIVE_CLIENT_PUSH_MESSAGE(0),
    // receive a gateway response, but request has timeout
    RECEIVE_GATEWAY_RESPONSE_REQUEST_TIMEOUT(1),
    // receive an error gateway response
    RECEIVE_ERROR_GATEWAY_RESPONSE(2),
    // receive client ack, but task timeout
    RECEIVE_CLIENT_ACK_TASK_TIMEOUT(3),
    // push message to client success, but gateway connection is closed
    PUSH_MESSAGE_TO_CLIENT_SUCCESS_GATEWAY_CONNECTION_CLOSED(4),
    // push message to client failure, but gateway connection is closed
    PUSH_MESSAGE_TO_CLIENT_FAILURE_GATEWAY_CONNECTION_CLOSED(5),
    // push message to client offline, but gateway connection is closed
    PUSH_MESSAGE_TO_CLIENT_OFFLINE_GATEWAY_CONNECTION_CLOSED(6),
    // push message to client redirect, but gateway connection is closed
    PUSH_MESSAGE_TO_CLIENT_REDIRECT_GATEWAY_CONNECTION_CLOSED(7),
    // push message to client timeout
    PUSH_MESSAGE_TO_CLIENT_TIMEOUT(8),
    // one ack context was rejected
    PUSH_ACK_CONTEXT_REJECTED(9),
    // [SingleUserPush] push message to client timeout
    SINGLE_USER_PUSH_MESSAGE_TO_CLIENT_TIMEOUT(10),
    // [SingleUserPush] find local router but conn disconnected
    SINGLE_USER_PUSH_FIND_LOCAL_ROUTER_CONN_DISCONNECTED(11),
    // [SingleUserPush] push message to client failure, tcp sender too busy
    SINGLE_USER_PUSH_MESSAGE_TO_CLIENT_FAILURE_TCP_SENDER_TOO_BUSY(12),
    // [SingleUserPush] remote router not exists user offline
    SINGLE_USER_PUSH_REMOTE_ROUTER_NOT_EXISTS_USER_OFFLINE(13),
    // [SingleUserPush] find remote router in this pc, but local router not exists
    SINGLE_USER_PUSH_FIND_REMOTE_ROUTER_THIS_PC_LOCAL_ROUTER_NOT_EXISTS(14),
    // [SingleUserPush] find router in another pc
    SINGLE_USER_PUSH_FIND_ROUTER_IN_ANOTHER_PC(15),
    // [SingleUserPush] push message to client success
    SINGLE_USER_PUSH_MESSAGE_TO_CLIENT_SUCCESS(16),
    // [SingleUserPush] push message to client failure
    SINGLE_USER_PUSH_MESSAGE_TO_CLIENT_FAILURE(17),
    // [SingleUserPush] client ack success
    SINGLE_USER_PUSH_CLIENT_ACK_SUCCESS(18),
    // [SingleUserPush] client ack timeout
    SINGLE_USER_PUSH_CLIENT_ACK_TIMEOUT(19),
    // broadcast to client finish, but gateway connection is closed
    BROADCAST_TO_CLIENT_FINISH_GATEWAY_CONNECTION_CLOSED(19),
    // [Broadcast] find router in local but conn disconnect
    BROADCAST_FIND_ROUTER_IN_LOCAL_CONN_DISCONNECT(20),
    // [Broadcast] task finished
    BROADCAST_TASK_FINISHED(21),
    // [Broadcast] push message to client success
    BROADCAST_PUSH_MESSAGE_TO_CLIENT_SUCCESS(22),
    // [Broadcast] push message to client failure
    BROADCAST_PUSH_MESSAGE_TO_CLIENT_FAILURE(23),
    // client ack success, but gateway connection is closed
    CLIENT_ACK_SUCCESS_GATEWAY_CONNECTION_CLOSED(24),
    // send ack for push message
    SEND_ACK_FOR_PUSH_MESSAGE(25);

    private Integer value;

    DetailTypes(int i) {
        this.value = i;
    }

    public Integer getValue() {
        return value;
    }
}
