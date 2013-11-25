package com.ctrip.framework.cdashboard.engine.command;

/**
 * Abstract command response, do some common logic
 * User: huang_jie
 * Date: 11/22/13
 * Time: 10:37 AM
 */
public abstract class AbstractCommandResponse implements CommandResponse{
    private ResultCode resultCode;
    private String resultInfo;

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(String resultInfo) {
        this.resultInfo = resultInfo;
    }
}
