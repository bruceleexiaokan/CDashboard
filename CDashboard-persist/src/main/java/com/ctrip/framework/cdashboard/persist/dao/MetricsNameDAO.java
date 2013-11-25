package com.ctrip.framework.cdashboard.persist.dao;

/**
 * Metrics name data access object
 * User: huang_jie
 * Date: 11/22/13
 * Time: 11:10 AM
 */
public interface MetricsNameDAO {
    /**
     * Get metric name id based on namespace and name
     *
     * @param namespace
     * @param metricsName
     * @return
     */
    public int getMetricsNameID(String namespace, String metricsName);
}
