package com.mpush.test.util;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.mpush.tools.log.DetailTypes;

import java.util.Arrays;
import java.util.Optional;

public class LogDbType extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        Optional<Object> detailType = Arrays.stream(event.getArgumentArray()).filter(o -> o instanceof DetailTypes).findFirst();
        detailType.ifPresent(dType -> {
            // switch ((DetailTypes)dType) {
            //     case PUSH_ACK_CONTEXT_REJECTED :
            //         System.out.println("--------");
            //         break;
            // }
        });
        return "[db]";
    }
}