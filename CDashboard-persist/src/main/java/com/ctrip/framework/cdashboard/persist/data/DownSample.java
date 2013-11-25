package com.ctrip.framework.cdashboard.persist.data;

import java.util.Map;

/**
 * Common down sample logic for each down sample function
 * User: huang_jie
 * Date: 11/22/13
 * Time: 2:56 PM
 */
public abstract class DownSample {
    public Map<Byte,Double> downSampled;
    public byte index;
    public long lastDataPointTime;
    public long interval;
    public int maxNum;
    public long baseTime;
    public abstract void downSample(double value, long timestamp);
}
