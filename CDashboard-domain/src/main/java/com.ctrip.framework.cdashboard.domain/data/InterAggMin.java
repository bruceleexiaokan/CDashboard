package com.ctrip.framework.cdashboard.domain.data;

/**
 * User: huang_jie
 * Date: 11/4/13
 * Time: 4:48 PM
 */
public class InterAggMin implements InterAgg {
    private double min;
    private boolean hasValue;
    @Override
    public void aggregate(double value) {
        if (!hasValue) {
            this.min =value;
            this.hasValue = true;
        }
        if (this.min > value) {
            this.min = value;
        }
    }

    @Override
    public double getValue() {
        return this.min;
    }
}
