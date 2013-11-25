package com.ctrip.framework.cdashboard.engine.command;

/**
 * Command response interface
 * User: huang_jie
 * Date: 11/21/13
 * Time: 11:05 AM
 */
public interface CommandResponse {
    /**
     * If command process success
     *
     * @return true success else failure
     */
    public boolean isSuccess();

    /**
     * Build command process result
     *
     * @return json format result data
     */
    public String build();
}
