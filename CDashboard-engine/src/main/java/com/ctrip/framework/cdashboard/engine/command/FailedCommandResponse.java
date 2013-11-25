package com.ctrip.framework.cdashboard.engine.command;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONObject;

/**
 * Common failure command response
 */
public class FailedCommandResponse implements CommandResponse {
    private ResultCode resultCode = ResultCode.SERVER_INTERNAL_ERROR;
    private String resultInfo;
    private Throwable t;

    public FailedCommandResponse(ResultCode resultCode, String resultInfo) {
        this.resultCode = resultCode;
        this.resultInfo = resultInfo;
    }

    public FailedCommandResponse(ResultCode resultCode, String resultInfo, Throwable t) {
        this.resultCode = resultCode;
        this.resultInfo = resultInfo;
        this.t = t;
    }

    @Override
    public boolean isSuccess() {
        if (resultCode == ResultCode.SUCCESS) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String build() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result-code", resultCode.resultCode);
        jsonObject.put("result-info", resultInfo);
        if (t != null) {
            jsonObject.put("root-cause", ExceptionUtils.getStackTrace(t));
        }
        return jsonObject.toString();
    }

}
