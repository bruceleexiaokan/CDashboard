package com.ctrip.framework.cdashboard.domain.data;

/**
 * User: huang_jie
 * Date: 11/4/13
 * Time: 4:48 PM
 */
public class InterAggMax implements InterAgg {
    private double max;
    private boolean hasValue;

    @Override
    public void aggregate(double value) {
        if (!hasValue) {
            this.max = value;
            this.hasValue = true;
        }
        if (this.max < value) {
            this.max = value;
        }
    }

    @Override
    public double getValue() {
        return this.max;
    }
}
