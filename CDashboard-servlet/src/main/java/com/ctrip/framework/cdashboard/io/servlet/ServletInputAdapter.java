package com.ctrip.framework.cdashboard.io.servlet;

import com.ctrip.framework.cdashboard.common.io.CommandName;
import com.ctrip.framework.cdashboard.common.io.InputAdapter;

import java.io.InputStream;

/**
 * Input adapter servlet implement
 * User: huang_jie
 * Date: 11/21/13
 * Time: 1:31 PM
 */
public class ServletInputAdapter implements InputAdapter {
    private InputStream inputStream;
    private CommandName commandName;

    public ServletInputAdapter(InputStream inputStream, CommandName commandName) {
        this.inputStream = inputStream;
        this.commandName = commandName;
    }

    @Override
    public CommandName getCommandName() {
        return this.commandName;
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }
}
