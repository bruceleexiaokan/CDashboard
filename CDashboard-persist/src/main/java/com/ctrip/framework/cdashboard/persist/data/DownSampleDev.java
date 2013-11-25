package com.ctrip.framework.cdashboard.persist.data;

import java.util.HashMap;

/**
 * Dev down sample function
 * User: huang_jie
 * Date: 11/25/13
 * Time: 10:40 AM
 */
public class DownSampleDev extends DownSample {
    private int count;
    private double sum;
    private double dev;

    public DownSampleDev(int rtDPNum, long baseTime, long interval) {
        this.maxNum = rtDPNum;
        this.baseTime = baseTime;
        this.interval = interval;
        this.index = (byte) -1;
        downSampled = new HashMap<Byte, Double>();
    }

    @Override
    public void downSample(double val, long timestamp) {
        byte dataPointIndex = (byte) ((timestamp - baseTime) / interval);
        if (index == dataPointIndex) {
            double c = this.count + 1;
            double k = (this.sum + val) / c;
            double m = (this.sum / this.count) - k;
            double n = val - k;
            this.dev = this.dev + this.count * m * m + n * n;
            this.sum += val;
            this.count++;
            downSampled.put(dataPointIndex, this.dev / this.count);
        } else {
            this.index = dataPointIndex;
            this.count = 1;
            this.sum = val;
            this.dev = 0;
        }
    }
}
