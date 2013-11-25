package com.ctrip.framework.cdashboard.persist.dao.hbase;

import com.ctrip.framework.cdashboard.hbase.plugin.filter.TagsRowFilter;
import com.ctrip.framework.cdashboard.persist.dao.DataFragment;
import com.ctrip.framework.cdashboard.persist.data.DataPointStream;
import com.ctrip.framework.cdashboard.persist.data.DownSampleStream;
import com.ctrip.framework.cdashboard.persist.data.TimeRange;
import com.ctrip.framework.cdashboard.persist.util.Bytes;
import com.ctrip.framework.cdashboard.persist.util.TimeRangeSplitUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class HBaseDataFragment implements DataFragment {
    private TreeMap<Short, TreeSet<Integer>> filterTags;
    private TimeRange timeRange;
    private int mid;
    private HTableInterface table;
    private HBaseDataPointStream dps;
    private List<Scan> scans;
    private Scan curScan;
    private DownSampleStream downsampleStream;

    public HBaseDataFragment(int mid, TimeRange timeRange, HTableInterface table,
                             TreeMap<Short, TreeSet<Integer>> filterTags, DownSampleStream downsampleStream) {
        this.table = table;
        this.downsampleStream = downsampleStream;
        scans = new ArrayList<Scan>();

        this.mid = mid;
        this.filterTags = filterTags;
        if (this.filterTags != null) {
            Iterator<Entry<Short, TreeSet<Integer>>> it = this.filterTags.entrySet().iterator();
            while (it.hasNext()) {
                TreeSet<Integer> val = it.next().getValue();
                if (val == null || val.size() == 0) {
                    it.remove();
                } else if (val != null && val.size() == 1 && val.contains(0)) {
                    it.remove();
                }
            }
            if (this.filterTags.size() == 0) {
                this.filterTags = null;
            }
        }
        this.timeRange = timeRange;
    }

    @Override
    public DataPointStream getTimeSeriesResultFragment() throws IOException {
        if (dps != null) {
            return dps;
        }
        if (timeRange == null) {
            throw new IllegalStateException("Time range has not been set.");
        }
        buildScanList(buildRowFilter(), timeRange);
        dps = new HBaseDataPointStream(table, scans, timeRange, downsampleStream);
        return dps;
    }

    private void buildScanList(Filter filter, TimeRange timeRange) {
        long startTime = timeRange.startTime;
        long endTime = timeRange.endTime;
        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(startTime);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(endTime);
        int startYear = calStart.get(Calendar.YEAR);
        int startMonth = calStart.get(Calendar.MONTH);
        int endYear = calEnd.get(Calendar.YEAR);
        int endMonth = calEnd.get(Calendar.MONTH);
        while (startYear != endYear || startMonth != endMonth) {
            clearExceptMonth(calEnd);
            buildScans(calEnd.getTimeInMillis(), endTime, filter);
            endTime = calEnd.getTimeInMillis() - 1;
            calEnd.setTimeInMillis(endTime);
            endYear = calEnd.get(Calendar.YEAR);
            endMonth = calEnd.get(Calendar.MONTH);
        }
        buildScans(startTime, calEnd.getTimeInMillis(), filter);
    }

    private void clearExceptMonth(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void buildScan(long startTime, long endTime, Filter filter, int[] qualifiers) {
        byte[] startRow = Bytes.add(new byte[]{0, 0}, Bytes.toBytes(mid), TimeRangeSplitUtil.getTimeParts(endTime));
        byte[] stopRow = Bytes.add(new byte[]{0, 0}, Bytes.toBytes(mid), TimeRangeSplitUtil.getTimeParts(startTime));
        stopRow = Bytes.add(stopRow, Bytes.toBytes(Short.MAX_VALUE));
        stopRow = Bytes.add(stopRow, new byte[]{0});
        if (curScan == null || !org.apache.hadoop.hbase.util.Bytes.startsWith(curScan.getStartRow(), startRow)
                || !org.apache.hadoop.hbase.util.Bytes.startsWith(curScan.getStopRow(), stopRow)) {
            curScan = new Scan();
            curScan.setCaching(4096);
            curScan.setCacheBlocks(true);
            if (filter != null) {
                TagsRowFilter curFilter = ((TagsRowFilter) filter).copy();
                if (ArrayUtils.isNotEmpty(qualifiers)) {
                    curFilter.setSecondOffsets(qualifiers);
                    curScan.setFilter(curFilter);
                } else if (curFilter.tagsFilter != null) {
                    curScan.setFilter(curFilter);
                }
            }
            curScan.setStartRow(startRow);
            curScan.setStopRow(stopRow);

            scans.add(curScan);
        }
    }

    private void buildScans(long startTime, long endTime, Filter filter) {
        long perRowTime = 4096000L;
        long start = startTime % perRowTime;
        long end = endTime % perRowTime;
        long tempStartTime = startTime + perRowTime - start;
        long tempEndTime = endTime - end;
        if (end > 0) {
            if (tempEndTime <= startTime) {
                int[] qualifiers = TimeRangeSplitUtil.getQualifiers(startTime, endTime);
                if (filter == null) {
                    filter = new TagsRowFilter();
                }
                buildScan(startTime, endTime, filter, qualifiers);
            } else {
                int[] qualifiers = TimeRangeSplitUtil.getQualifiers(tempEndTime, endTime);
                if (filter == null) {
                    filter = new TagsRowFilter();
                }
                buildScan(tempEndTime, endTime, filter, qualifiers);
            }
        }
        if (tempEndTime > tempStartTime) {
            buildScan(tempStartTime, tempEndTime - 1, filter, null);
        }
        if (start >= 0) {
            if (tempStartTime >= endTime) {
                int[] qualifiers = TimeRangeSplitUtil.getQualifiers(startTime, endTime);
                if (filter == null) {
                    filter = new TagsRowFilter();
                }
                buildScan(startTime, endTime, filter, qualifiers);
            } else {
                int[] qualifiers = TimeRangeSplitUtil.getQualifiers(startTime, tempStartTime - 1);
                if (filter == null) {
                    filter = new TagsRowFilter();
                }
                buildScan(startTime, tempStartTime - 1, filter, qualifiers);
            }
        }

    }

    private Filter buildRowFilter() {
        if (filterTags == null || filterTags.size() == 0) {
            return null;
        }
        TagsRowFilter rt = new TagsRowFilter();
        rt.setTagsFilterInfo(filterTags);
        return rt;
    }

}
