package com.ctrip.framework.cdashboard.domain.data;

/**
 * User: huang_jie
 * Date: 11/4/13
 * Time: 4:48 PM
 */
public class InterAggDev implements InterAgg {
    private double sum;
    private double dev;
    private int count;
    private boolean hasValue = false;

    @Override
    public void aggregate(double value) {
        if (!hasValue) {
            this.sum += value;
            this.count++;
            this.hasValue = true;
        } else {
            double oldAvg = this.sum / count;
            double newAvg = (this.sum + value) / (this.count + 1);
            this.dev = this.dev + this.count * (oldAvg - newAvg) * (oldAvg - newAvg) + (value - newAvg) * (value - newAvg);
            this.count++;
            this.sum += value;
        }
    }

    @Override
    public double getValue() {
        return this.dev;
    }
}
