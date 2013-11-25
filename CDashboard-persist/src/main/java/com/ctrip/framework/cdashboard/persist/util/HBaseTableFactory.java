package com.ctrip.framework.cdashboard.persist.util;

import com.ctrip.framework.cdashboard.common.config.Configure;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * User: huang_jie
 * Date: 11/22/13
 * Time: 4:25 PM
 */
public class HBaseTableFactory {
    private static Map<String, HTablePool> nsTableCache = new ConcurrentHashMap<String, HTablePool>();
    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Get HBase table interface by namespace, related HBase configure from database
     *
     * @param namespace
     * @return
     */
    public static HTableInterface getHBaseTable(String namespace) {
        HTablePool tablePool = getHBaseTablePool(namespace);
        String tableName = Configure.getStringByXPath("namespaces/namespace[name = '" + namespace + "']/table");
        return tablePool.getTable(tableName);
    }

    public static synchronized void close(String poolKey) throws IOException {
        HTablePool hTablePool = nsTableCache.remove(poolKey);
        if (hTablePool != null) {
            hTablePool.close();
        }
    }

    public static void shutdown() throws IOException {
        for (String key : nsTableCache.keySet()) {
            close(key);
        }
    }

    /**
     * Get HBase table pool by namespace, related HBase configure from database
     *
     * @param namespace
     * @return
     */
    public static HTablePool getHBaseTablePool(String namespace) {
        HTablePool tablePool = nsTableCache.get(namespace);
        if (tablePool == null) {
            lock.writeLock().lock();
            try {
                tablePool = nsTableCache.get(namespace);
                if (tablePool == null) {
                    Configuration conf = HBaseConfiguration.create();
                    conf.set(HConstants.ZOOKEEPER_QUORUM, Configure.getString("hbaseConfig.zookeeper"));
                    conf.set(HConstants.ZOOKEEPER_ZNODE_PARENT, Configure.getString("hbaseConfig.basePath"));
                    tablePool = new HTablePool(conf, 15);
                    if (tablePool != null) {
                        nsTableCache.put(namespace, tablePool);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return tablePool;
    }
}
