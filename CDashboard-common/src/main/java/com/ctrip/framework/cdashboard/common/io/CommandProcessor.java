package com.ctrip.framework.cdashboard.common.io;

/**
 * Command processor interface
 * User: huang_jie
 * Date: 11/21/13
 * Time: 2:16 PM
 */
public interface CommandProcessor {
    /**
     * Process command, read request data from input adapter, write response data into output adapter
     *
     * @param input
     * @param output
     */
    public void processCommand(InputAdapter input, OutputAdapter output);
}
