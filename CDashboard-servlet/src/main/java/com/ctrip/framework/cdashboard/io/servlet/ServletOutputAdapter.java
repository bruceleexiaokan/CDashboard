package com.ctrip.framework.cdashboard.io.servlet;

import com.ctrip.framework.cdashboard.common.io.OutputAdapter;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Output adapter servlet implement
 * User: huang_jie
 * Date: 11/21/13
 * Time: 1:57 PM
 */
public class ServletOutputAdapter implements OutputAdapter {
    private AsyncContext context;
    private InputStream responseStream;
    private OutputStream os;
    private AtomicReference<IOException> error;

    public ServletOutputAdapter(AsyncContext context) throws IOException {
        this.context = context;
        context.setTimeout(600000L);
        context.addListener(new AsyncServletListener(this));
        HttpServletResponse resp = (HttpServletResponse) context.getResponse();
        resp.setCharacterEncoding("UTF-8");
        this.os = resp.getOutputStream();
        this.error = new AtomicReference<IOException>();
    }

    public void setError(IOException e) {
        this.error.getAndSet(e);
    }

    @Override
    public void setOutputStream(InputStream responseStream) {
        this.responseStream = responseStream;
    }

    @Override
    public void flush() throws IOException {
        IOException ex = error.get();
        if (ex == null) {
            if (responseStream != null) {
                byte[] buf = new byte[1024];
                int len = responseStream.read(buf);
                while (len > 0) {
                    os.write(buf, 0, len);
                    len = responseStream.read(buf);
                }
                os.flush();
            }
        } else {
            throw ex;
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        os.close();
        context.complete();
    }
}
