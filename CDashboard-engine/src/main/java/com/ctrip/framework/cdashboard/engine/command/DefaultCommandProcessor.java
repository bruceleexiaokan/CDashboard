package com.ctrip.framework.cdashboard.engine.command;

import com.ctrip.framework.cdashboard.common.io.CommandName;
import com.ctrip.framework.cdashboard.common.io.CommandProcessor;
import com.ctrip.framework.cdashboard.common.io.InputAdapter;
import com.ctrip.framework.cdashboard.common.io.OutputAdapter;
import com.ctrip.framework.cdashboard.engine.Engine;
import com.ctrip.framework.cdashboard.engine.command.data.GetGroupedDataPointsCommand;
import com.google.common.base.Charsets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

/**
 * Default command processor
 * User: huang_jie
 * Date: 11/21/13
 * Time: 4:47 PM
 */
public class DefaultCommandProcessor implements CommandProcessor {
    /**
     * Process command in different thread pool
     *
     * @param input
     * @param output
     */
    @Override
    public void processCommand(InputAdapter input, OutputAdapter output) {
        CommandResponse resp = null;
        Command command;
        try {
            command = generateCommand(input, output);
            if (command != null) {
                CommandTask task = new CommandTask(command);
                if (command.isThrift()) {
                    Engine.getInstance().getThriftThreadPool().submit(task);
                } else if (command.isHighCost()) {
                    Engine.getInstance().getHighCostThreadPool().submit(task);
                } else {
                    Engine.getInstance().getCommandThreadPool().submit(task);
                }
            } else {
                resp = new FailedCommandResponse(ResultCode.INVALID_COMMAND, "This command is not support.");
            }
        } catch (RejectedExecutionException e) {
            resp = new FailedCommandResponse(ResultCode.SERVER_BUSY, "Server is busy now, please retry later.", e);
        } catch (Throwable e) {
            resp = new FailedCommandResponse(ResultCode.SERVER_INTERNAL_ERROR, "Process command error.", e);
        }
        if (resp != null) {
            byte[] response = resp.build().getBytes(Charsets.UTF_8);
            output.setOutputStream(new ByteArrayInputStream(response));
            try {
                output.flush();
                output.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Generate command based on input
     *
     * @param input
     * @param output
     * @return
     */
    private Command generateCommand(InputAdapter input, OutputAdapter output) {
        Command command = null;
        if (CommandName.GET_GROUPED_DATA_POINTS == input.getCommandName()) {
            command = new GetGroupedDataPointsCommand(input, output);
        }
        return command;
    }

}
