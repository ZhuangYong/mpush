package com.mpush.tools.log;

/**
 * 日志详细类型
 */
public enum Steps {
    PUSH_ADD_TASK("PUSH_ADD_TASK"),
    PUSH_SUCCESS("PUSH_SUCCESS"),
    ;

    private String value;

    Steps(String i) {
        this.value = i;
    }

    public String getValue() {
        return value;
    }
}
