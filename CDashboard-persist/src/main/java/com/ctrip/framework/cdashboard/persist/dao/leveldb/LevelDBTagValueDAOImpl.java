package com.ctrip.framework.cdashboard.persist.dao.leveldb;

import com.ctrip.framework.cdashboard.common.config.Configure;
import com.ctrip.framework.cdashboard.common.util.StringUtil;
import com.ctrip.framework.cdashboard.domain.data.IDType;
import com.ctrip.framework.cdashboard.domain.data.TimeSeriesQuery;
import com.ctrip.framework.cdashboard.persist.cache.LevelDB;
import com.ctrip.framework.cdashboard.persist.dao.MetaDAOFactory;
import com.ctrip.framework.cdashboard.persist.dao.TagNameDAO;
import com.ctrip.framework.cdashboard.persist.dao.TagValueDAO;
import com.ctrip.framework.cdashboard.persist.exception.QueryException;
import com.ctrip.framework.cdashboard.persist.util.Bytes;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * {@link LevelDBTagNameDAOImpl} level db implement, read data from level db
 * User: huang_jie
 * Date: 11/22/13
 * Time: 2:00 PM
 */
public class LevelDBTagValueDAOImpl implements TagValueDAO {
    private LevelDB tagValueLevelDB;
    private TagNameDAO tagNameDAO;

    private LevelDBTagValueDAOImpl() {
        String path = Configure.getString("tagValueCache.path", "/opt/cache/dashboard/tagvalus/");
        int size = Configure.getInt("tagValueCache.size", 32);
        tagValueLevelDB = LevelDB.getInstance();
        tagValueLevelDB.open(path, size * 1024 * 1024);

        tagNameDAO = MetaDAOFactory.getTagNameDAO();
    }

    /**
     * Get tag name and tag value id pair based on metrics name and time series query condition from level db
     *
     * @param mid
     * @param query
     * @return
     */
    @Override
    public TreeMap<Short, TreeSet<Integer>> getQueryTagIds(int mid, TimeSeriesQuery query) {
        TreeMap<Short, TreeSet<Integer>> filterTagIds = new TreeMap<Short, TreeSet<Integer>>();
        Map<String, Set<String>> filterTags = query.getFilterTags();
        for (Map.Entry<String, Set<String>> entry : filterTags.entrySet()) {
            String tagName = entry.getKey();
            short tagNameId = tagNameDAO.getTagNameID(mid, tagName);
            filterTagIds.put(tagNameId, getTagValueIds(mid, tagNameId, entry.getValue()));
        }
        return filterTagIds;
    }

    /**
     * Get tag value ids based on metrics name id, tag name id and tag value string patten from level db
     *
     * @param mid
     * @param tagNameID
     * @param tagValuePatten
     * @return
     */
    @Override
    public Set<Integer> getTagValueIds(int mid, short tagNameID, String tagValuePatten) {
        Set<Integer> tagValueIds = new TreeSet<Integer>();
        /*add not exist tag value id, when input not exist tag value, it can filter data,
         if not add this value, it can get all data.*/
        tagValueIds.add(0);
        byte[] prefix = Bytes.add(Bytes.toBytes(IDType.TAG_VALUE.reverse), Bytes.toBytes(mid), Bytes.toBytes(tagNameID));
        if (!tagValuePatten.contains("*")) {
            prefix = Bytes.add(prefix, Bytes.toBytes(tagValuePatten));
            byte[] value = tagValueLevelDB.get(prefix);
            if (value != null) {
                tagValueIds.add(Bytes.toInt(value, 0, 4));
            }
            return tagValueIds;
        }
        String[] tagValues = tagValuePatten.split("\\*");
        boolean startWith = false;
        boolean endWith = false;
        prefix = Bytes.add(prefix, Bytes.toBytes(tagValues[0]));
        if (tagValuePatten.startsWith("*")) {
            startWith = true;
        }
        if (tagValuePatten.endsWith("*")) {
            endWith = true;
        }
        Map<byte[], byte[]> tagValueMap = tagValueLevelDB.seek(prefix, 0);
        for (Map.Entry<byte[], byte[]> tagValueEntry : tagValueMap.entrySet()) {
            byte[] tvKey = tagValueEntry.getKey();
            String tv = Bytes.toString(tvKey, 7, tvKey.length - 7);
            if (StringUtil.tagValueMatch(tv, tagValues, startWith, endWith)) {
                tagValueIds.add(Bytes.toInt(tagValueEntry.getValue(), 0, 4));
            }
        }
        return tagValueIds;
    }

    /**
     * Get tag value ids based on metrics name id, tag name id and tag value string pattens from level db
     *
     * @param mid
     * @param tagNameID
     * @param tagValuePattens
     * @return
     */
    @Override
    public TreeSet<Integer> getTagValueIds(int mid, short tagNameID, Set<String> tagValuePattens) {
        TreeSet<Integer> tagValueIds = new TreeSet<Integer>();
        Iterator<String> tagValuesIt = tagValuePattens.iterator();
        String values = "";
        while (tagValuesIt.hasNext()) {
            String tagValue = tagValuesIt.next();
            values += tagValue + ",";
            if (StringUtils.isBlank(tagValue)) {
                continue;
            }
            tagValueIds.addAll(getTagValueIds(mid, tagNameID, tagValue));
        }
        if (tagValueIds.size() > 0 && tagValueIds.size() == 1) {
            throw new QueryException("Can not found related tag value id for tag value: " + values);
        }
        return tagValueIds;
    }

    /**
     * Get tag value by mid, tag name id and tag value id from level db
     *
     * @param mid
     * @param tagNameID
     * @param tagValueID
     * @return
     */
    @Override
    public String getTagValue(int mid, short tagNameID, int tagValueID) {
        byte[] key = Bytes.add(Bytes.toBytes(IDType.TAG_VALUE.forward), Bytes.toBytes(mid), Bytes.toBytes(tagNameID));
        key = Bytes.add(key, Bytes.toBytes(tagValueID));
        byte[] value = tagValueLevelDB.get(key);
        if (value == null) {
            return null;
        }
        return Bytes.toString(value);
    }

    private static class LevelDBTagValueDAOImplHolder {
        private static LevelDBTagValueDAOImpl instance = new LevelDBTagValueDAOImpl();
    }

    public static LevelDBTagValueDAOImpl getInstance() {
        return LevelDBTagValueDAOImplHolder.instance;
    }

}
