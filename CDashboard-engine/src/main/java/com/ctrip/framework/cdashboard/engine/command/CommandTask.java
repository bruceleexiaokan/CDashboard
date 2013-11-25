package com.ctrip.framework.cdashboard.engine.command;

/**
 * Command task implement, be executed by different thread pool executor
 * User: huang_jie
 * Date: 11/21/13
 * Time: 4:54 PM
 */
public class CommandTask implements Runnable {
    private Command command;

    public CommandTask(Command command) {
        this.command = command;
    }

    /**
     * Execute command logic
     */
    @Override
    public void run() {
        command.execute();
    }
}
