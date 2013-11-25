package com.ctrip.framework.cdashboard.persist.cache;

import com.ctrip.framework.cdashboard.common.util.FileUtil;
import com.ctrip.framework.cdashboard.persist.util.Bytes;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Access Level DB utility class
 * User: huang_jie
 * Date: 11/22/13
 * Time: 12:35 PM
 */
public class LevelDB {
    private static final Logger LOGGER = LoggerFactory.getLogger(LevelDB.class);
    private Options options = null;
    private DB db = null;
    private boolean isOpen = false;
    private String dbPath = "";

    /**
     * Get {@link LevelDB} instance
     *
     * @return
     */
    public static LevelDB getInstance() {
        return new LevelDB();
    }

    public void setPath(String path) {
        this.dbPath = path;
    }

    public boolean open(String path, int cacheSize) {
        setPath(path);

        options = new Options();
        options.cacheSize(cacheSize);
        options.createIfMissing(true);
        try {
            File file = new File(dbPath);
            if (!file.exists()) {
                if (!FileUtil.createNotExists(dbPath, "data")) {
                    return false;
                }
            }

            db = JniDBFactory.factory.open(new File(dbPath), options);
            if (db != null) {
                isOpen = true;
            }
        } catch (Exception e) {
            LOGGER.error("Open LevelDB Error: " + e.getMessage());
        }
        return isOpen;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        if (isOpen == true) {
            try {
                db.close();
            } catch (IOException e) {
                LOGGER.error("Close LevelDB Error: " + e.getMessage());
            }
            isOpen = false;
        }
    }

    /**
     * Put key value pair data into level db
     *
     * @param key
     * @param value
     */
    public void put(byte[] key, byte[] value) {
        if (key == null || value == null) {
            return;
        }
        db.put(key, value);
    }

    /**
     * Get data by key from level db
     *
     * @param key
     * @return
     */
    public byte[] get(byte[] key) {
        return db.get(key);
    }

    public String getPath() {
        return dbPath;
    }

    /**
     * Delete data by key
     *
     * @param key
     */
    public void delete(byte[] key) {
        db.delete(key);
    }

    public void destroy() {
        try {
            JniDBFactory.factory.destroy(new File(dbPath), options);
        } catch (IOException e) {
            LOGGER.error("Destroy LevelDB Error: " + e.getMessage());
        }
    }

    /**
     * Seek data from start key
     *
     * @param startKey
     * @param limit    limit return data size
     * @return
     */
    public Map<byte[], byte[]> seek(byte[] startKey, int limit) {
        Map<byte[], byte[]> result = new TreeMap<byte[], byte[]>(Bytes.BYTES_COMPARATOR);
        int len = startKey.length;
        DBIterator iterator = db.iterator();
        try {
            iterator.seek(startKey);
            int count = 0;
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                byte[] key = entry.getKey();
                byte[] value = entry.getValue();
                if (Bytes.compareTo(key, 0, len, startKey, 0, len) != 0 || (limit > 0 && limit < count)) {
                    break;
                }
                result.put(key, value);
                count++;
            }
        } finally {
            if (null != iterator) {
                try {
                    iterator.close();
                } catch (Exception e) {
                    LOGGER.warn("Close level db iterator error: ", e);
                }
            }
        }
        return result;
    }

}
