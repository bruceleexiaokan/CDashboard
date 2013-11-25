package com.ctrip.framework.cdashboard.persist.dao.hbase;

import com.ctrip.framework.cdashboard.domain.data.TimeSeriesQuery;
import com.ctrip.framework.cdashboard.persist.dao.DataFragment;
import com.ctrip.framework.cdashboard.persist.dao.DataPointDAO;
import com.ctrip.framework.cdashboard.persist.data.DataPointStream;
import com.ctrip.framework.cdashboard.persist.data.DownSampleStream;
import com.ctrip.framework.cdashboard.persist.data.TimeRange;
import com.ctrip.framework.cdashboard.persist.data.TimeSeriesResultSet;
import com.ctrip.framework.cdashboard.persist.util.HBaseTableFactory;
import org.apache.hadoop.hbase.client.HTableInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * {@link DataPointDAO} HBase implement
 * User: huang_jie
 * Date: 11/22/13
 * Time: 3:58 PM
 */
public class HBaseDataPointDAOImpl implements DataPointDAO {
    private TimeSeriesQuery query;
    private int mid;
    private TimeRange timeRange;
    private TreeMap<Short, TreeSet<Integer>> filterTags;
    private DownSampleStream downsampleStream;

    public HBaseDataPointDAOImpl(TimeSeriesQuery query, int mid, TimeRange timeRange,
                                 TreeMap<Short, TreeSet<Integer>> filterTags,
                                 DownSampleStream downsampleStream) {
        this.query = query;
        this.mid = mid;
        this.timeRange = timeRange;
        this.filterTags = filterTags;
        this.downsampleStream = downsampleStream;
    }

    /**
     * Get data point HBase read stream
     *
     * @return
     */
    @Override
    public DataPointStream getDataPointStream() {
        List<DataFragment> fragments = new ArrayList<DataFragment>();
        HTableInterface table = HBaseTableFactory.getHBaseTable(query.getNameSpace());
        HBaseDataFragment fragment = new HBaseDataFragment(mid,timeRange,table,filterTags,downsampleStream);
        fragments.add(fragment);
        return new TimeSeriesResultSet(fragments);
    }
}
