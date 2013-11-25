package com.ctrip.framework.cdashboard.engine.command.data;

import com.ctrip.framework.cdashboard.domain.data.AggregatorType;
import com.ctrip.framework.cdashboard.domain.data.DownSampleType;
import com.ctrip.framework.cdashboard.domain.data.TimeSeriesQuery;
import com.ctrip.framework.cdashboard.engine.constant.EngineConstant;
import org.json.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Get grouped data points request, read request data from byte stream,
 * then parse json data to build request object
 * User: huang_jie
 * Date: 11/21/13
 * Time: 11:05 AM
 */
public class GetGroupedDataPointsRequest {
    private TimeSeriesQuery timeSeriesQuery;
    private Set<String> groupBy = new HashSet<String>();
    private AggregatorType aggregator;
    private DownSampleType downSampler;
    private int maxDataPointCount = EngineConstant.MAX_POINT_COUNT;
    private long startTime;
    private long endTime;
    private String interval;
    private String callback;

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public TimeSeriesQuery getTimeSeriesQuery() {
        return timeSeriesQuery;
    }

    public void setTimeSeriesQuery(TimeSeriesQuery timeSeriesQuery) {
        this.timeSeriesQuery = timeSeriesQuery;
    }

    public void addGroupByTag(String tag) {
        groupBy.add(tag);
    }

    public Set<String> getGroupByTags() {
        return groupBy;
    }

    public AggregatorType getAggregator() {
        return aggregator;
    }

    public void setAggregator(AggregatorType aggregator) {
        this.aggregator = aggregator;
    }

    public DownSampleType getDownSampler() {
        return downSampler;
    }

    public void setDownSampler(DownSampleType downSampler) {
        this.downSampler = downSampler;
    }

    public int getMaxDataPointCount() {
        return maxDataPointCount;
    }

    public void setMaxDataPointCount(int maxDataPointCount) {
        this.maxDataPointCount = maxDataPointCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    /**
     * Parse json string data, then build {@link GetGroupedDataPointsRequest} object
     *
     * @param jsonObj
     * @return
     */
    public static GetGroupedDataPointsRequest parse(JSONObject jsonObj) {
        GetGroupedDataPointsRequest rt = new GetGroupedDataPointsRequest();
        rt.setCallback(jsonObj.optString("callback"));
        JSONObject tsObj = jsonObj.getJSONObject("time-series");
        rt.setTimeSeriesQuery(TimeSeriesQuery.parseFromJson(tsObj));
        Set<String> keySet = jsonObj.keySet();
        if (keySet.contains("group-by")) {
            if (!jsonObj.isNull("group-by")) {
                JSONArray tagArray = jsonObj.getJSONArray("group-by");
                for (int i = 0; i < tagArray.length(); i++) {
                    if (!tagArray.isNull(i)) {
                        String groupByTag = tagArray.getString(i);
                        rt.addGroupByTag(groupByTag);
                        rt.getTimeSeriesQuery().addFilterTagValue(groupByTag, null);
                    }
                }
            }
        }
        if(keySet.contains("interval")){
            rt.setInterval(jsonObj.getString("interval"));
        }
        if (keySet.contains("aggregator")) {
            if (!jsonObj.isNull("aggregator")) {
                rt.setAggregator(AggregatorType.value(jsonObj.getString("aggregator")));
            }
        }
        if (keySet.contains("downsampler")) {
            if (!jsonObj.isNull("downsampler")) {
                rt.setDownSampler(DownSampleType.value(jsonObj.getString("downsampler")));
            }
        }
        if (keySet.contains("max-point-count")) {
            int maxDataPointCount = jsonObj.getInt("max-point-count");
            if (maxDataPointCount > 0 && maxDataPointCount <= EngineConstant.MAX_POINT_COUNT) {
                rt.setMaxDataPointCount(maxDataPointCount);
            }
        }
        SimpleDateFormat time_format = new SimpleDateFormat(EngineConstant.TIMESTAMP_FORMAT);
        String startTime = jsonObj.getString("start-time");
        try {
            Date timestamp = time_format.parse(startTime);
            rt.setStartTime(timestamp.getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Parse start time error:" + startTime, e);
        }
        String endTime = jsonObj.getString("end-time");
        try {
            Date timestamp = time_format.parse(endTime);
            rt.setEndTime(timestamp.getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Parse end time error:" + startTime, e);
        }

        return rt;
    }

}
