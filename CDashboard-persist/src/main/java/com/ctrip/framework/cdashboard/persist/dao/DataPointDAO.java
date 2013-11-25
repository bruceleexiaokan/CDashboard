package com.ctrip.framework.cdashboard.persist.dao;

import com.ctrip.framework.cdashboard.persist.data.DataPointStream;

/**
 * Data point data access object
 * User: huang_jie
 * Date: 11/22/13
 * Time: 2:07 PM
 */
public interface DataPointDAO {
    /**
     * Get data point read stream
     *
     * @return
     */
    public DataPointStream getDataPointStream();
}
