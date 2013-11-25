package com.ctrip.framework.cdashboard.persist.data;

import java.io.IOException;

/**
 * Data point read stream
 * User: huang_jie
 * Date: 11/22/13
 * Time: 2:09 PM
 */
public interface DataPointStream {
    /**
     * Do down sample from data stream
     *
     * @throws IOException
     */
    public void downSample() throws IOException;

    /**
     * Close this data stream when finish down sample
     */
    public void close();
}
