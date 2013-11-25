package com.ctrip.framework.cdashboard.domain.data;

/**
 * User: huang_jie
 * Date: 11/4/13
 * Time: 4:48 PM
 */
public class InterAggAvg implements InterAgg {
    private double sum;
    private int count;

    @Override
    public void aggregate(double value) {
        this.sum += value;
        this.count++;
    }

    @Override
    public double getValue() {
        if (this.count > 0) {
            return this.sum / this.count;
        }
        return 0;
    }
}
