package com.ctrip.framework.cdashboard.common.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Output response adapter interface
 * User: huang_jie
 * Date: 11/21/13
 * Time: 1:26 PM
 */
public interface OutputAdapter {
    /**
     * Set input stream for response output stream
     *
     * @param responseStream
     */
    public void setOutputStream(InputStream responseStream);

    /**
     * Flush data from response input stream to io output stream
     *
     * @throws IOException
     */
    public void flush() throws IOException;

    /**
     * Close response input stream
     *
     * @throws IOException
     */
    public void close() throws IOException;
}
