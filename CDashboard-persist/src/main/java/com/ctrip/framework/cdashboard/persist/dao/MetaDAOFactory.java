package com.ctrip.framework.cdashboard.persist.dao;

import com.ctrip.framework.cdashboard.persist.dao.leveldb.LevelDBMetricsNameDAOImpl;
import com.ctrip.framework.cdashboard.persist.dao.leveldb.LevelDBTagNameDAOImpl;
import com.ctrip.framework.cdashboard.persist.dao.leveldb.LevelDBTagValueDAOImpl;

/**
 * All meta dao create instance factory
 * User: huang_jie
 * Date: 11/22/13
 * Time: 11:10 AM
 */
public class MetaDAOFactory {
    /**
     * Get {@link MetricsNameDAO} implement instance
     *
     * @return
     */
    public static MetricsNameDAO getMetricsNameDAO() {
        return LevelDBMetricsNameDAOImpl.getInstance();
    }

    /**
     * Get {@link TagNameDAO} implement instance
     *
     * @return
     */
    public static TagNameDAO getTagNameDAO() {
        return LevelDBTagNameDAOImpl.getInstance();
    }

    /**
     * Get {@link TagValueDAO} implement instance
     *
     * @return
     */
    public static TagValueDAO getTagValueDAO() {
        return LevelDBTagValueDAOImpl.getInstance();
    }
}
