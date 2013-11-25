package com.ctrip.framework.cdashboard.engine.command.data;

import com.ctrip.framework.cdashboard.common.io.InputAdapter;
import com.ctrip.framework.cdashboard.common.io.OutputAdapter;
import com.ctrip.framework.cdashboard.common.util.StringUtil;
import com.ctrip.framework.cdashboard.domain.data.*;
import com.ctrip.framework.cdashboard.engine.command.AbstractCommand;
import com.ctrip.framework.cdashboard.engine.command.FailedCommandResponse;
import com.ctrip.framework.cdashboard.engine.command.ResultCode;
import com.ctrip.framework.cdashboard.engine.constant.EngineConstant;
import com.ctrip.framework.cdashboard.persist.dao.*;
import com.ctrip.framework.cdashboard.persist.dao.hbase.HBaseDataPointDAOImpl;
import com.ctrip.framework.cdashboard.persist.data.DataPointStream;
import com.ctrip.framework.cdashboard.persist.data.DownSample;
import com.ctrip.framework.cdashboard.persist.data.DownSampleStream;
import com.ctrip.framework.cdashboard.persist.data.TimeRange;
import com.ctrip.framework.cdashboard.persist.util.Bytes;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Get grouped data points command, each request build one command
 * User: huang_jie
 * Date: 11/21/13
 * Time: 4:49 PM
 */
public class GetGroupedDataPointsCommand extends AbstractCommand {
    private final static Logger LOGGER = LoggerFactory.getLogger(GetGroupedDataPointsCommand.class);
    private GetGroupedDataPointsRequest request;
    private GetGroupedDataPointsResponse response;
    private long baseTime;// base time is start time
    private long endTime;
    private long interval;
    private int returnPointsCount;
    private int mid;

    public GetGroupedDataPointsCommand(InputAdapter input, OutputAdapter output) {
        super(input, output);
    }

    @Override
    protected void process() {
        TimeSeriesQuery query = request.getTimeSeriesQuery();
        MetricsNameDAO metricsNameDAO = MetaDAOFactory.getMetricsNameDAO();
        mid = metricsNameDAO.getMetricsNameID(query.getNameSpace(), query.getMetricsName());
        if (mid == 0) {
            commandResponse = new FailedCommandResponse(ResultCode.SUCCESS_BUT_NO_DATA, "Metrics not exist.");
            return;
        }
        response = new GetGroupedDataPointsResponse();
        response.setCallback(request.getCallback());

        TagNameDAO tagNameDAO = MetaDAOFactory.getTagNameDAO();
        TagValueDAO tagValueDAO = MetaDAOFactory.getTagValueDAO();
        Set<String> groupTags = request.getGroupByTags();
        int groupTagNum = 0;
        Map<Short, String> groupTagIds = null;
        if (CollectionUtils.isNotEmpty(groupTags)) {
            groupTagIds = new TreeMap<Short, String>();
            for (String tagName : groupTags) {
                short tagNameId = tagNameDAO.getTagNameID(mid, tagName);
                if (tagNameId == 0) {
                    commandResponse = new FailedCommandResponse(ResultCode.INVALID_GROUP, "Group tag name <" + tagName + "> not found, please check!");
                    return;
                }
                groupTagIds.put(tagNameId, tagName);
            }
            groupTagNum = groupTags.size();
        }
        DownSampleStream downsampleStream = new DownSampleStream(request.getDownSampler(), baseTime, interval, returnPointsCount);
        TimeRange timeRange = new TimeRange();
        timeRange.startTime = baseTime;
        timeRange.endTime = endTime;
        TreeMap<Short, TreeSet<Integer>> filterTags = tagValueDAO.getQueryTagIds(mid, query);
        DataPointDAO dataPointDAO = new HBaseDataPointDAOImpl(query, mid, timeRange, filterTags, downsampleStream);
        DataPointStream stream = dataPointDAO.getDataPointStream();
        if (stream != null) {
            try {
                stream.downSample();
            } catch (IOException e) {
                LOGGER.warn("Load data point from HBase error:", e);
            } finally {
                stream.close();
            }
        }
        Map<String, GroupedDataPoints> groupedDataPointsMap = new HashMap<String, GroupedDataPoints>();
        if (groupTagNum > 0) {
            Set<Short> groupTagNameIdSet = groupTagIds.keySet();
            Short[] groupedTagNames = groupTagNameIdSet.toArray(new Short[groupTagNameIdSet.size()]);
            byte[][] groupTagNameIds = new byte[groupTagNum][];
            int tagIndex = 0;
            for (short groupTagNid : groupTagNameIdSet) {
                groupTagNameIds[tagIndex] = Bytes.toBytes(groupTagNid);
                tagIndex++;
            }
            int[] tagValues = new int[groupTagNum];
            for (Map.Entry<byte[], DownSample> entry : downsampleStream.downSampleMap.entrySet()) {
                byte[] tags = entry.getKey();
                String key = "";
                int gidx = 0;
                int pos = 0;
                while (pos + 6 <= tags.length && gidx < groupTagNum) {
                    if (tags[pos] == groupTagNameIds[gidx][0]
                            && tags[pos + 1] == groupTagNameIds[gidx][1]) {
                        tagValues[gidx] = Bytes.toInt(tags, pos + 2, 4);
                        key += groupedTagNames[gidx] + "=" + tagValues[gidx];
                        gidx++;
                    }
                    pos += 6;
                }
                if (gidx == groupTagNameIds.length) {
                    GroupedDataPoints groupedDataPoints = groupedDataPointsMap.get(key);
                    if (groupedDataPoints == null) {
                        groupedDataPoints = new GroupedDataPoints(returnPointsCount);
                        groupedDataPoints.baseTime = baseTime;
                        groupedDataPoints.interval = request.getInterval();
                        response.addGroupedDataPoints(groupedDataPoints);
                        groupedDataPoints.tsids = new LinkedList<byte[]>();
                        for (int i = 0; i < groupedTagNames.length; i++) {
                            short tagNameId = groupedTagNames[i];
                            groupedDataPoints.group.put(tagNameDAO.getTagName(mid, tagNameId), tagValueDAO.getTagValue(mid, tagNameId, tagValues[i]));
                        }
                    }
                    groupedDataPoints.tsids.add(tags);
                    groupedDataPointsMap.put(key, groupedDataPoints);
                }
            }
        } else {
            GroupedDataPoints groupedDataPoints = new GroupedDataPoints(returnPointsCount);
            groupedDataPoints.baseTime = baseTime;
            groupedDataPoints.interval = request.getInterval();
            ;
            response.addGroupedDataPoints(groupedDataPoints);
            groupedDataPoints.tsids = new LinkedList<byte[]>();
            groupedDataPoints.tsids.addAll(downsampleStream.downSampleMap.keySet());
            groupedDataPointsMap.put(null, groupedDataPoints);
        }

        aggGroups(groupedDataPointsMap, downsampleStream);
        calRate(groupedDataPointsMap);
        response.setResultCode(ResultCode.SUCCESS);
        response.setResultInfo("success.");
        SimpleDateFormat time_format = new SimpleDateFormat(EngineConstant.TIMESTAMP_FORMAT);
        response.setBaseTime(time_format.format(new Date(baseTime)));

        response.setResultCode(ResultCode.SUCCESS);
        response.setResultInfo("success");
        commandResponse = response;
    }

    private void calRate(Map<String, GroupedDataPoints> groupInfos) {
        if (request.getDownSampler() != DownSampleType.RATE) {
            return;
        }
        for (Map.Entry<String, GroupedDataPoints> entry : groupInfos.entrySet()) {
            GroupedDataPoints groupInfo = entry.getValue();
            InterAgg preValue = groupInfo.aggregatorInfos[0];
            for (int j = 1; j < groupInfo.aggregatorInfos.length; j++) {
                InterAgg interAggInfo = groupInfo.aggregatorInfos[j];
                if (preValue == null && interAggInfo == null) {
                } else {
                    InterAgg aggInfo = new InterAggSum();
                    aggInfo.aggregate((interAggInfo == null ? 0d : interAggInfo.getValue()) + (preValue == null ? 0d : preValue.getValue()));
                    groupInfo.aggregatorInfos[j - 1] = aggInfo;
                }
                preValue = interAggInfo;
            }
            groupInfo.isRate = true;
            this.baseTime += interval;
            groupInfo.baseTime = baseTime;
        }
    }


    private void aggGroups(Map<String, GroupedDataPoints> groupInfos, DownSampleStream downsampleStream) {
        for (Map.Entry<String, GroupedDataPoints> workEntry : groupInfos.entrySet()) {
            GroupedDataPoints groupInfo = workEntry.getValue();
            for (byte[] tsId : groupInfo.tsids) {
                DownSample downSampleInfo = downsampleStream.downSampleMap.get(tsId);
                if (groupInfo.lastDataPointTime < downSampleInfo.lastDataPointTime) {
                    groupInfo.lastDataPointTime = downSampleInfo.lastDataPointTime;
                }
                if (downSampleInfo.downSampled != null && downSampleInfo.downSampled.size() > 0) {
                    for (Map.Entry<Byte, Double> entry : downSampleInfo.downSampled.entrySet()) {
                        byte index = entry.getKey();
                        InterAgg interAggInfo = groupInfo.aggregatorInfos[index];
                        if (interAggInfo == null) {
                            switch (request.getAggregator()) {
                                case SUM:
                                    interAggInfo = new InterAggSum();
                                    break;
                                case MAX:
                                    interAggInfo = new InterAggMax();
                                    break;
                                case MIN:
                                    interAggInfo = new InterAggMin();
                                    break;
                                case DEV:
                                    interAggInfo = new InterAggDev();
                                    break;
                                case AVG:
                                    interAggInfo = new InterAggAvg();
                                    break;
                                default:
                                    throw new RuntimeException("Not support this aggregator method: " + request.getAggregator());
                            }
                            groupInfo.aggregatorInfos[index] = interAggInfo;
                        }
                        interAggInfo.aggregate(downSampleInfo.downSampled.get(index));
                    }
                }
            }
            groupInfo.tsids.clear();
        }
    }

    /**
     * Check this request whether high cost
     *
     * @return
     */
    @Override
    public boolean isHighCost() {
        if (request == null) {
            parseRequest();
        }
        if (request != null && isLongTimeRequest()) {
            return true;
        }
        return false;
    }

    /**
     * Parse json data from input stream, and build {@link GetGroupedDataPointsRequest} object
     */
    @Override
    protected void parseRequest() {
        try {
            request = GetGroupedDataPointsRequest.parse(jsonObject);
        } catch (Exception e) {
            commandResponse = new FailedCommandResponse(ResultCode.INVALID_COMMAND, "Can not parse request data from input stream.", e);
            return;
        }

        baseTime = request.getStartTime();
        if (baseTime <= 0) {
            commandResponse = new FailedCommandResponse(ResultCode.INVALID_START_TIME, "The start time in invalid.");
            return;
        }
        endTime = request.getEndTime();
        if (endTime < baseTime) {
            commandResponse = new FailedCommandResponse(ResultCode.INVALID_END_TIME, "The end time in invalid.");
            return;
        }
        int maxPointsCount = request.getMaxDataPointCount();

        this.interval = StringUtil.parseInterval(request.getInterval());
        if (request.getDownSampler() == DownSampleType.RATE) {
            this.baseTime = baseTime - this.interval;
            maxPointsCount += 1;
        }
        if (baseTime + this.interval * maxPointsCount < endTime) {
            endTime = baseTime + this.interval * maxPointsCount;
        }
        returnPointsCount = (int) ((endTime - baseTime) / interval);
        //re calc end time based on interval and base time
        if (request.getDownSampler() != DownSampleType.RATE) {
            endTime = baseTime + this.interval * returnPointsCount;
        }
        long re = (endTime - baseTime) % interval;
        if (request.getDownSampler() == DownSampleType.RATE && re > 0) {
            returnPointsCount += 1;
        }
    }

    /**
     * If the request during one day, it is a long time request
     *
     * @return
     */

    private boolean isLongTimeRequest() {
        if (endTime > 0 && baseTime > 0 && (endTime - baseTime > 3600000 * 24)) {
            return true;
        }
        return false;
    }

}
