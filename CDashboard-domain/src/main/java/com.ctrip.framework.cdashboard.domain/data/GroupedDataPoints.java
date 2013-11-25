package com.ctrip.framework.cdashboard.domain.data;

import org.json.JSONException;
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GroupedDataPoints {
    public Map<String, String> group;
    public long baseTime;
    public String interval;
    public long lastDataPointTime = -1;
    public InterAgg[] aggregatorInfos;
    public List<byte[]> tsids;
    public boolean isRate = false;
    public GroupedDataPoints(int retDPCount) {
        aggregatorInfos = new InterAgg[retDPCount];
        group = new HashMap<String, String>(3);
    }
    public void buildJson(JSONStringer builder) throws JSONException {
        builder.object();
        if (group != null) {
            builder.key("time-series-group");
            builder.object();
            for (Entry<String, String> entry : group.entrySet()) {
                builder.key(entry.getKey()).value(entry.getValue());
            }
            builder.endObject();
        }
        if (aggregatorInfos != null) {
            builder.key("data-points");
            builder.object();
            SimpleDateFormat time_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            builder.key("base-time").value(time_format.format(new Date(baseTime)));
            builder.key("interval").value(interval);
            builder.key("last_datapoint_ts").value(lastDataPointTime / 1000);
            builder.key("value-type").value("double");
            builder.key("data-points");
            builder.array();
            if (aggregatorInfos != null) {
                for (InterAgg aggregatorInfo : aggregatorInfos) {
                    if (aggregatorInfo == null) {
                        builder.value(null);
                    } else {
                        builder.value(aggregatorInfo.getValue());
                    }
                }
            }
            builder.endArray();
            builder.endObject();
        }
        builder.endObject();
    }

}
