package com.ctrip.framework.cdashboard.persist.dao;

/**
 * Metrics tag name data access object
 * User: huang_jie
 * Date: 11/22/13
 * Time: 1:54 PM
 */
public interface TagNameDAO {
    /**
     * Get metrics tag name id based on metrics name id and tag name
     *
     * @param mid
     * @param tagName
     * @return
     */
    public short getTagNameID(int mid, String tagName);

    /**
     * Get tag name by mid and tag name id
     *
     * @param mid
     * @param tagNameId
     * @return
     */
    public String getTagName(int mid, short tagNameId);
}
