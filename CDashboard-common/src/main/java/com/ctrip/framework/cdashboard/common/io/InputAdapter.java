package com.ctrip.framework.cdashboard.common.io;

import java.io.InputStream;

/**
 * Input request adapter interface
 * User: huang_jie
 * Date: 11/21/13
 * Time: 1:24 PM
 */
public interface InputAdapter {
    /**
     * Get command name
     *
     * @return
     */
    public CommandName getCommandName();

    /**
     * Get user input request stream
     *
     * @return
     */
    public InputStream getInputStream();
}
