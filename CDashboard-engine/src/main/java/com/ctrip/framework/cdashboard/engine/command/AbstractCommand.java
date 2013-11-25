package com.ctrip.framework.cdashboard.engine.command;

import com.ctrip.framework.cdashboard.common.io.InputAdapter;
import com.ctrip.framework.cdashboard.common.io.OutputAdapter;
import com.google.common.base.Charsets;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Abstract command implement {@link Command} interface, do some common logic
 * User: huang_jie
 * Date: 11/21/13
 * Time: 6:07 PM
 */
public abstract class AbstractCommand implements Command {
    protected CommandResponse commandResponse = null;
    protected InputAdapter input;
    protected OutputAdapter output;
    protected JSONObject jsonObject;

    protected AbstractCommand(InputAdapter input, OutputAdapter output) {
        this.input = input;
        this.output = output;
        jsonObject = new JSONObject(new JSONTokener(input.getInputStream()));
    }

    @Override
    public boolean isHighCost() {
        return false;
    }

    @Override
    public boolean isThrift() {
        return false;
    }

    /**
     * Process command core logic
     */
    protected abstract void process();

    /**
     * Parse json data from input stream, and build related request object
     */
    protected abstract void parseRequest();

    /**
     * Execute command for common logic, such write data to client
     */
    @Override
    public void execute() {
        try {
            parseRequest();
            //may be some error happen when parse input stream, so flush error message direct
            if (commandResponse != null) {
                flushData();
                return;
            }

            process();
            if (commandResponse == null) {
                commandResponse = new FailedCommandResponse(ResultCode.SUCCESS_BUT_NO_DATA, "No data found");
            }
            flushData();
        } catch (Throwable e) {
            commandResponse = new FailedCommandResponse(ResultCode.SERVER_INTERNAL_ERROR, "Process command error: ",e);
            flushData();
        }
    }

    /**
     * Flush data to client
     */
    private void flushData() {
        byte[] value = commandResponse.build().getBytes(Charsets.UTF_8);
        output.setOutputStream(new ByteArrayInputStream(value));
        try {
            output.flush();
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
