package com.ctrip.framework.cdashboard.io.servlet;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.io.IOException;

/**
 * Async servlet listener implement
 * User: huang_jie
 * Date: 11/21/13
 * Time: 2:00 PM
 */
public class AsyncServletListener implements AsyncListener {
    private ServletOutputAdapter outputAdapter;

    AsyncServletListener(ServletOutputAdapter outputAdapter) {
        this.outputAdapter = outputAdapter;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {

    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        Throwable t = event.getThrowable();
        if (t != null) {
            outputAdapter.setError(new IOException("Async process timeout.", t));
        } else {
            outputAdapter.setError(new IOException("Async process timeout."));
        }

    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        Throwable t = event.getThrowable();
        if (t != null) {
            outputAdapter.setError(new IOException("Async process error.", t));
        } else {
            outputAdapter.setError(new IOException("Async process error."));
        }
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {

    }
}
