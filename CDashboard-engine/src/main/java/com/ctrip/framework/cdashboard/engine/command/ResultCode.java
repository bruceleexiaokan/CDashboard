package com.ctrip.framework.cdashboard.engine.command;

/**
 * Command execute result code type
 * User: huang_jie
 * Date: 11/21/13
 * Time: 5:50 PM
 */
public enum ResultCode {
    SUCCESS(0),
    SUCCESS_BUT_NO_DATA(1000),
    SERVER_BUSY(2002),
    SERVER_INTERNAL_ERROR(2001),
    INVALID_COMMAND(3000),
    INVALID_GROUP(3004),
    INVALID_START_TIME(3005),
    INVALID_END_TIME(3006);
    public final int resultCode;

    private ResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
}
