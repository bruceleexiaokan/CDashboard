package com.ctrip.framework.cdashboard.domain.data;

/**
 * Aggregator type
 * User: huang_jie
 * Date: 11/22/13
 * Time: 10:17 AM
 */
public enum AggregatorType {
    SUM("sum"),
    MAX("max"),
    MIN("min"),
    AVG("avg"),
    DEV("dev");

    public final String code;

    private AggregatorType(String code) {
        this.code = code;
    }

    public static AggregatorType value(String code) {
        AggregatorType rt;
        if (AggregatorType.SUM.code.equals(code)) {
            rt = AggregatorType.SUM;
        } else if (AggregatorType.MAX.code.equals(code)) {
            rt = AggregatorType.MAX;
        } else if (AggregatorType.MIN.code.equals(code)) {
            rt = AggregatorType.MIN;
        } else if (AggregatorType.AVG.code.equals(code)) {
            rt = AggregatorType.AVG;
        } else if (AggregatorType.DEV.code.equals(code)) {
            rt = AggregatorType.DEV;
        } else {
            throw new RuntimeException("Not support this aggregator type: " + code);
        }
        return rt;
    }
}
