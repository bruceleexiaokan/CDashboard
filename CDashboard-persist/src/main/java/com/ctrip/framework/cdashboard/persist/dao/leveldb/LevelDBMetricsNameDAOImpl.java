package com.ctrip.framework.cdashboard.persist.dao.leveldb;

import com.ctrip.framework.cdashboard.common.config.Configure;
import com.ctrip.framework.cdashboard.common.constant.NamespaceConstant;
import com.ctrip.framework.cdashboard.domain.data.IDType;
import com.ctrip.framework.cdashboard.persist.cache.LevelDB;
import com.ctrip.framework.cdashboard.persist.dao.MetricsNameDAO;
import com.ctrip.framework.cdashboard.persist.util.Bytes;
import org.apache.commons.lang.ArrayUtils;

/**
 * {@link MetricsNameDAO} level db implement, read data from level db
 * User: huang_jie
 * Date: 11/22/13
 * Time: 11:11 AM
 */
public class LevelDBMetricsNameDAOImpl implements MetricsNameDAO {
    private LevelDB metricLevelDB;

    public LevelDBMetricsNameDAOImpl() {
        String path = Configure.getString("metricNameCache.path", "/opt/cache/dashboard/metrics/");
        int size = Configure.getInt("metricNameCache.size", 32);
        metricLevelDB = LevelDB.getInstance();
        metricLevelDB.open(path, size * 1024 * 1024);
    }

    private static class LevelDBMetricsNameDAOImplHolder {
        private static LevelDBMetricsNameDAOImpl instance = new LevelDBMetricsNameDAOImpl();
    }

    public static LevelDBMetricsNameDAOImpl getInstance() {
        return LevelDBMetricsNameDAOImplHolder.instance;
    }

    /**
     * Get metric name id based on namespace and name from level db
     *
     * @param namespace
     * @param metricsName
     * @return
     */
    @Override
    public int getMetricsNameID(String namespace, String metricsName) {
        if (namespace == null || namespace.length() == 0) {
            namespace = NamespaceConstant.DEFAULT_NAMESPACE;
        }
        String key = IDType.METRIC.reverse + NamespaceConstant.NAMESPACE_SPLIT
                + namespace + NamespaceConstant.NAMESPACE_SPLIT + metricsName;
        byte[] id = metricLevelDB.get(Bytes.toBytes(key));
        if (ArrayUtils.isEmpty(id)) {
            return 0;
        }
        return Bytes.toInt(id, 0, 4);
    }
}
