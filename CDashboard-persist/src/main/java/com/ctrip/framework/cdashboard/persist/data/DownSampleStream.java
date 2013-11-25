package com.ctrip.framework.cdashboard.persist.data;

import com.ctrip.framework.cdashboard.domain.data.DownSampleType;
import com.ctrip.framework.cdashboard.persist.util.Bytes;

import java.util.TreeMap;

/**
 * Down sample stream, include all time series down sample data
 * User: huang_jie
 * Date: 11/22/13
 * Time: 2:53 PM
 */
public class DownSampleStream {
    private DownSampleType downSampleType;
    private final long baseTime;
    private final long interval;
    private final int returnPointsCount;
    public TreeMap<byte[], DownSample> downSampleMap;

    public DownSampleStream(DownSampleType downSampleType, long baseTime, long interval, int returnPointsCount) {
        this.baseTime = baseTime;
        this.interval = interval;
        this.returnPointsCount = returnPointsCount;
        this.downSampleMap = new TreeMap<byte[], DownSample>(Bytes.BYTES_COMPARATOR);
        this.downSampleType = downSampleType;
    }

    public void downSample(byte[] tags, long timestamp, double value) {
        DownSample interDSInfo = downSampleMap.get(tags);
        if (interDSInfo == null) {
            switch (downSampleType) {
                case SUM:
                    interDSInfo = new DownSampleSum(returnPointsCount, baseTime, interval);
                    break;
                case MAX:
                    interDSInfo = new DownSampleMax(returnPointsCount, baseTime, interval);
                    break;
                case MIN:
                    interDSInfo = new DownSampleMin(returnPointsCount, baseTime, interval);
                    break;
                case DEV:
                    interDSInfo = new DownSampleDev(returnPointsCount, baseTime, interval);
                    break;
                case AVG:
                    interDSInfo = new DownSampleAvg(returnPointsCount, baseTime, interval);
                    break;
                case RATE:
                    interDSInfo = new DownSampleRate(returnPointsCount, baseTime, interval);
                    break;
                default:
                    throw new RuntimeException("Not support this down sample type: " + downSampleType);
            }
            downSampleMap.put(tags, interDSInfo);
        }
        if (timestamp > interDSInfo.lastDataPointTime) {
            interDSInfo.lastDataPointTime = timestamp;
        }
        interDSInfo.downSample(value, timestamp);
    }
}
