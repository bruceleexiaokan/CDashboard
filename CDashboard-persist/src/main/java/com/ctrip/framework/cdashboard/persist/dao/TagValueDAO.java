package com.ctrip.framework.cdashboard.persist.dao;

import com.ctrip.framework.cdashboard.domain.data.TimeSeriesQuery;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Metrics tag value data access object
 * User: huang_jie
 * Date: 11/22/13
 * Time: 1:55 PM
 */
public interface TagValueDAO {
    /**
     * Get tag name and tag value id pair based on metrics name and time series query condition
     *
     * @param mid
     * @param query
     * @return
     */
    public TreeMap<Short, TreeSet<Integer>> getQueryTagIds(int mid, TimeSeriesQuery query);

    /**
     * Get tag value ids based on metrics name id, tag name id and tag value string patten
     *
     * @param mid
     * @param tagNameID
     * @param tagValuePatten
     * @return
     */
    public Set<Integer> getTagValueIds(int mid, short tagNameID, String tagValuePatten);

    /**
     * Get tag value ids based on metrics name id, tag name id and tag value string pattens
     *
     * @param mid
     * @param tagNameID
     * @param tagValuePattens
     * @return
     */
    public TreeSet<Integer> getTagValueIds(int mid, short tagNameID, Set<String> tagValuePattens);

    /**
     * Get tag value by mid, tag name id and tag value id
     *
     * @param mid
     * @param tagNameID
     * @param tagValueID
     * @return
     */
    public String getTagValue(int mid, short tagNameID, int tagValueID);

}
