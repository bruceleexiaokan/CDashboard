package com.ctrip.framework.cdashboard.persist.data;

import java.util.HashMap;

/**
 * Avg down sample function
 * User: huang_jie
 * Date: 11/25/13
 * Time: 10:40 AM
 */
public class DownSampleAvg extends DownSample {
    private double sum;
    private double count;

    public DownSampleAvg(int rtDPNum, long baseTime, long interval) {
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
            this.sum += val;
            this.count++;
            downSampled.put(dataPointIndex, this.sum / this.count);
        } else {
            this.index = dataPointIndex;
            this.sum = val;
            this.count = 1;
        }
    }
}
