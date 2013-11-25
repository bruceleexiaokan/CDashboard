package com.ctrip.framework.cdashboard.persist.data;

import java.util.HashMap;

/**
 * Min down sample function
 * User: huang_jie
 * Date: 11/25/13
 * Time: 10:40 AM
 */
public class DownSampleMin extends DownSample {

    public DownSampleMin(int rtDPNum, long baseTime, long interval) {
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
            if (downSampled.get(dataPointIndex) > val) {
                downSampled.put(dataPointIndex, val);
            }
        } else {
            this.index = dataPointIndex;
            downSampled.put(dataPointIndex, val);
        }
    }

}
