package com.ctrip.framework.cdashboard.domain.data;

/**
 * User: huang_jie
 * Date: 11/4/13
 * Time: 4:48 PM
 */
public interface InterAgg {
    public void aggregate(double value);

    public double getValue();
}
