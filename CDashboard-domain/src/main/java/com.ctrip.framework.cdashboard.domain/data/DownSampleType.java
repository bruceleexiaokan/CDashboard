package com.ctrip.framework.cdashboard.domain.data;

/**
 * Down sample type
 * User: huang_jie
 * Date: 11/22/13
 * Time: 10:17 AM
 */
public enum DownSampleType {
    SUM("sum"),
    MAX("max"),
    MIN("min"),
    AVG("avg"),
    DEV("dev"),
    RATE("rat");

    public final String code;

    private DownSampleType(String code) {
        this.code = code;
    }

    public static DownSampleType value(String code) {
        DownSampleType rt;
        if (DownSampleType.SUM.code.equals(code)) {
            rt = DownSampleType.SUM;
        } else if (DownSampleType.MAX.code.equals(code)) {
            rt = DownSampleType.MAX;
        } else if (DownSampleType.MIN.code.equals(code)) {
            rt = DownSampleType.MIN;
        } else if (DownSampleType.AVG.code.equals(code)) {
            rt = DownSampleType.AVG;
        } else if (DownSampleType.DEV.code.equals(code)) {
            rt = DownSampleType.DEV;
        } else if (DownSampleType.RATE.code.equals(code)) {
            rt = DownSampleType.RATE;
        } else {
            throw new RuntimeException("Not support this aggregator type: " + code);
        }
        return rt;
    }
}
