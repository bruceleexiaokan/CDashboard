package com.ctrip.framework.cdashboard.domain.data;

import com.ctrip.framework.cdashboard.common.constant.NamespaceConstant;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Time series query domain object
 * User: huang_jie
 * Date: 11/22/13
 * Time: 9:56 AM
 */
public class TimeSeriesQuery {
    private String nameSpace;
    private String metricsName;
    //filter tags include tag name and tag values pair
    private Map<String, Set<String>> filterTags = new HashMap<String, Set<String>>();

    public String getNameSpace() {
        if (StringUtils.isBlank(nameSpace)) {
            return NamespaceConstant.DEFAULT_NAMESPACE;
        }
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getMetricsName() {
        return metricsName;
    }

    public void setMetricsName(String metricsName) {
        this.metricsName = metricsName;
    }

    public void addFilterTagValue(String tag, String value) {
        Set<String> stored = filterTags.get(tag);
        if (stored == null) {
            stored = new HashSet<String>();
            filterTags.put(tag, stored);
        }
        if (StringUtils.isNotBlank(value)) {
            stored.add(value);
        }
    }

    public Map<String, Set<String>> getFilterTags() {
        return filterTags;
    }

    public void setFilterTags(Map<String, Set<String>> filterTags) {
        this.filterTags = filterTags;
    }

    /**
     * Parse json string, then build {@link TimeSeriesQuery} Object
     *
     * @param jsonObj
     * @return
     */
    public static TimeSeriesQuery parseFromJson(JSONObject jsonObj) {
        TimeSeriesQuery rt = new TimeSeriesQuery();
        rt.setNameSpace(StringUtils.lowerCase(StringUtils.trim(jsonObj.optString("namespace", null))));
        rt.setMetricsName(StringUtils.lowerCase(StringUtils.trim(jsonObj.optString("metrics-name", null))));
        Set<String> keys = jsonObj.keySet();
        JSONObject tagsObj = null;
        if (keys.contains("tags")) {
            tagsObj = jsonObj.optJSONObject("tags");
        }
        if (tagsObj != null) {
            Iterator<String> it = tagsObj.keys();
            while (it.hasNext()) {
                String tag = it.next();
                if (StringUtils.isBlank(tag)) {
                    continue;
                }
                JSONArray valueArray = tagsObj.optJSONArray(tag);
                if (valueArray != null && valueArray.length() != 0) {
                    for (int i = 0; i < valueArray.length(); i++) {
                        rt.addFilterTagValue(StringUtils.trim(tag), StringUtils.trim(valueArray.optString(i, null)));
                    }
                }
            }
        }
        return rt;
    }

}
