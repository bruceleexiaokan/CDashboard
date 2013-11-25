package com.ctrip.framework.cdashboard.engine.command.data;

import com.ctrip.framework.cdashboard.domain.data.GroupedDataPoints;
import com.ctrip.framework.cdashboard.engine.command.AbstractCommandResponse;
import org.json.JSONStringer;

import java.util.LinkedList;
import java.util.List;

/**
 * Get grouped data points response, transfer object to json string
 * User: huang_jie
 * Date: 11/22/13
 * Time: 10:36 AM
 */
public class GetGroupedDataPointsResponse extends AbstractCommandResponse {
    private List<GroupedDataPoints> groupedDataPointsList = new LinkedList<GroupedDataPoints>();
    private String baseTime;
    private String callback;

    public String getCallback() {
        return callback;
    }

    public List<GroupedDataPoints> getGroupedDataPointsList() {
        return groupedDataPointsList;
    }

    public void setGroupedDataPointsList(List<GroupedDataPoints> groupedDataPointsList) {
        this.groupedDataPointsList = groupedDataPointsList;
    }

    public String getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(String baseTime) {
        this.baseTime = baseTime;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
    public void addGroupedDataPoints(GroupedDataPoints groupedDataPoints) {
        groupedDataPointsList.add(groupedDataPoints);
    }


    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public String build() {
        JSONStringer builder = new JSONStringer();
        builder.object();
        builder.key("result-code").value(getResultCode().resultCode);
        builder.key("result-info").value(getResultInfo());
        builder.key("time-series-group-list");
        builder.array();
        for (GroupedDataPoints groupedDataPoints : groupedDataPointsList) {
            groupedDataPoints.buildJson(builder);
        }
        builder.endArray();
        builder.endObject();
        return builder.toString();
    }
}
