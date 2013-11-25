package com.ctrip.framework.cdashboard.domain.data;

/**
 * User: huang_jie
 * Date: 11/4/13
 * Time: 4:48 PM
 */
public class InterAggSum implements InterAgg {
    private double value;

    @Override
    public void aggregate(double value) {
        this.value += value;

    }

    @Override
    public double getValue() {
        return this.value;
    }
}
