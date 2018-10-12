package com.mpush.core.push;

import com.mpush.api.spi.push.IPushMessage;
import com.mpush.core.ack.AckCallback;
import com.mpush.core.ack.AckTask;
import com.mpush.tools.common.TimeLine;
import com.mpush.tools.log.DetailTypes;
import com.mpush.tools.log.Logs;

public final class PushAckCallback implements AckCallback {
    private final IPushMessage message;
    private final TimeLine timeLine;
    private final PushCenter pushCenter;

    public PushAckCallback(IPushMessage message, TimeLine timeLine, PushCenter pushCenter) {
        this.message = message;
        this.timeLine = timeLine;
        this.pushCenter = pushCenter;
    }

    @Override
    public void onSuccess(AckTask task) {
        pushCenter.getPushListener().onAckSuccess(message, timeLine.successEnd().getTimePoints());
        Logs.PUSH.info("[SingleUserPush] client ack success. timeLine={}, task={}, message={}, dType={}", timeLine, task, message, DetailTypes.PUSH_MESSAGE_ACK);
    }

    @Override
    public void onTimeout(AckTask task) {
        pushCenter.getPushListener().onTimeout(message, timeLine.timeoutEnd().getTimePoints());
        Logs.PUSH.warn("[SingleUserPush] client ack timeout. timeLine={}, task={}, message={}, dType={}", timeLine, task, message, DetailTypes.PUSH_MESSAGE_ACK);
    }
}