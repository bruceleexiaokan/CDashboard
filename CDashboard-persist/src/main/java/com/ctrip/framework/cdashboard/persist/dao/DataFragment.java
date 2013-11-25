package com.ctrip.framework.cdashboard.persist.dao;

import com.ctrip.framework.cdashboard.persist.data.DataPointStream;

import java.io.IOException;

/**
 * User: huang_jie
 * Date: 11/22/13
 * Time: 4:01 PM
 */
public interface DataFragment {
    public DataPointStream getTimeSeriesResultFragment() throws IOException;
}
