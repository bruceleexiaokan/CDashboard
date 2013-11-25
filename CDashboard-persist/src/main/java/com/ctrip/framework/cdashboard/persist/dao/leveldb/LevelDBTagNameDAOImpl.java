package com.ctrip.framework.cdashboard.persist.dao.leveldb;

import com.ctrip.framework.cdashboard.common.config.Configure;
import com.ctrip.framework.cdashboard.domain.data.IDType;
import com.ctrip.framework.cdashboard.persist.cache.LevelDB;
import com.ctrip.framework.cdashboard.persist.dao.TagNameDAO;
import com.ctrip.framework.cdashboard.persist.util.Bytes;

/**
 * {@link LevelDBTagNameDAOImpl} level db implement, read data from level db
 * User: huang_jie
 * Date: 11/22/13
 * Time: 1:56 PM
 */
public class LevelDBTagNameDAOImpl implements TagNameDAO {
    private LevelDB tagNameLevelDB;

    private LevelDBTagNameDAOImpl() {
        String path = Configure.getString("tagNameCache.path", "/opt/cache/dashboard/tagnames/");
        int size = Configure.getInt("tagNameCache.size", 32);
        tagNameLevelDB = LevelDB.getInstance();
        tagNameLevelDB.open(path, size * 1024 * 1024);
    }

    /**
     * Get metrics tag name id based on metrics name id and tag name from level db
     *
     * @param mid
     * @param tagName
     * @return
     */
    @Override
    public short getTagNameID(int mid, String tagName) {
        byte[] key = Bytes.add(Bytes.toBytes(IDType.TAG_NAME.reverse), Bytes.toBytes(mid), Bytes.toBytes(tagName));
        byte[] value = tagNameLevelDB.get(key);
        if (value == null) {
            return 0;
        }
        return Bytes.toShort(value, 0, 2);
    }

    /**
     * Get tag name by mid and tag name id from level db
     *
     * @param mid
     * @param tagNameId
     * @return
     */
    @Override
    public String getTagName(int mid, short tagNameId) {
        byte[] key = Bytes.add(Bytes.toBytes(IDType.TAG_NAME.forward), Bytes.toBytes(mid), Bytes.toBytes(tagNameId));
        byte[] value = tagNameLevelDB.get(key);
        if (value == null) {
            return null;
        }
        return new String(value);
    }

    private static class LevelDBTagNameDAOImplHolder {
        private static LevelDBTagNameDAOImpl instance = new LevelDBTagNameDAOImpl();
    }

    public static LevelDBTagNameDAOImpl getInstance() {
        return LevelDBTagNameDAOImplHolder.instance;
    }

}
