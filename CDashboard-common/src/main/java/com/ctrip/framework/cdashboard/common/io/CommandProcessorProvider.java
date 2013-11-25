package com.ctrip.framework.cdashboard.common.io;

/**
 * Command processor provider
 * User: huang_jie
 * Date: 11/21/13
 * Time: 2:15 PM
 */
public class CommandProcessorProvider {

    private CommandProcessor commandProcessor;

    private CommandProcessorProvider() {
    }

    private static class CommandProcessorProviderHolder {
        private static CommandProcessorProvider instance = new CommandProcessorProvider();
    }

    public static CommandProcessorProvider getInstance() {
        return CommandProcessorProviderHolder.instance;
    }

    public CommandProcessor getCommandProcessor() {
        return commandProcessor;
    }

    public void setCommandProcessor(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }
}
