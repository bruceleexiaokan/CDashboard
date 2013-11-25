package com.ctrip.framework.cdashboard.common.io;

/**
 * User: huang_jie
 * Date: 11/21/13
 * Time: 1:42 PM
 */
public enum CommandName {
    GET_GROUPED_DATA_POINTS("getgroupeddatapoints"),
    PUT_DATA_POINTS("putdatapoints"),
    GET_MATE_DATA("getmetricstags");
    public final String command;

    private CommandName(String command) {
        this.command = command;
    }

    /**
     * Return the object represented by the code.
     */
    public static CommandName value(String cmd) {
        CommandName rt;
        if (CommandName.GET_GROUPED_DATA_POINTS.command.equals(cmd)) {
            rt = CommandName.GET_GROUPED_DATA_POINTS;
        } else if (CommandName.GET_MATE_DATA.command.equals(cmd)) {
            rt = CommandName.GET_MATE_DATA;
        } else if (CommandName.PUT_DATA_POINTS.command.equals(cmd)) {
            rt = CommandName.PUT_DATA_POINTS;
        }else {
            throw new RuntimeException("Not support this command: " + cmd);
        }
        return rt;
    }
}
