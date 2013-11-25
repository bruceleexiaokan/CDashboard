package com.ctrip.framework.cdashboard.engine.command;

/**
 * Command interface
 * User: huang_jie
 * Date: 11/21/13
 * Time: 11:28 AM
 */
public interface Command {
    /**
     * If this command is high cost request
     *
     * @return true high cost, else no
     */
    public boolean isHighCost();

    /**
     * If this command is batch extract big data
     *
     * @return
     */
    public boolean isThrift();

    /**
     * Execute command
     */
    public void execute();
}
