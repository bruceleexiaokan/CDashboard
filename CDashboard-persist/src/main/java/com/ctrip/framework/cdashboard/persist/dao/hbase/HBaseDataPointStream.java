package com.ctrip.framework.cdashboard.persist.dao.hbase;

import com.ctrip.framework.cdashboard.persist.data.DataPointStream;
import com.ctrip.framework.cdashboard.persist.data.DownSampleStream;
import com.ctrip.framework.cdashboard.persist.data.TimeRange;
import com.ctrip.framework.cdashboard.persist.util.Bytes;
import com.ctrip.framework.cdashboard.persist.util.HBaseClientUtil;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

import java.io.IOException;
import java.util.List;

/**
 * {@link DataPointStream} HBase implement
 * User: huang_jie
 * Date: 11/22/13
 * Time: 3:58 PM
 */
public class HBaseDataPointStream implements DataPointStream {
    private final HTableInterface table;
    private final TimeRange queryTimeRange;
    private ResultScanner resultScanner;
    private int curScanIndex = 0;
    private List<Scan> scans;
    private Scan curScan;
    private DownSampleStream downsampleStream;

    public HBaseDataPointStream(final HTableInterface table, final List<Scan> scans, final TimeRange queryTimeRange,
                                DownSampleStream downsampleStream) throws IOException {
        this.table = table;
        this.scans = scans;
        this.queryTimeRange = queryTimeRange;
        this.downsampleStream = downsampleStream;
    }


    @Override
    public void downSample() throws IOException {
        if (resultScanner == null) {
            curScan = scans.get(curScanIndex);
            resultScanner = table.getScanner(curScan);
        }
        Result result = resultScanner.next();
        while (result != null || curScanIndex < scans.size() - 1) {
            if (result == null) {
                HBaseClientUtil.closeResultScanner(resultScanner);
                curScanIndex++;
                if (curScanIndex != scans.size()) {
                    curScan = scans.get(curScanIndex);
                    resultScanner = table.getScanner(curScan);
                    result = resultScanner.next();
                }
                continue;
            }
            KeyValue[] rowKVs = result.raw();
            if (rowKVs != null) {
                processKVs(rowKVs);
            }
            result = resultScanner.next();
        }
    }


    @Override
    public void close() {
        HBaseClientUtil.closeResource(table, resultScanner);
    }

    private void processKVs(KeyValue[] kvs) {
        long baseTime = 0;
        byte[] nvs = null;
        byte[][] fdData = new byte[4096][];
        byte[] extfdData = null;
        for (KeyValue kv : kvs) {
            byte[] buffer = kv.getBuffer();
            if (baseTime == 0) {
                int rowOffset = kv.getRowOffset();
                short rowLength = kv.getRowLength();
                baseTime = (Integer.MAX_VALUE - (long)Bytes.toInt(buffer, rowOffset + 7, 4)) * 4096000;
                int tagsLen = rowLength - 12;
                nvs = new byte[rowLength - 12];
                for (int i = 0; i < tagsLen; i++) {
                    nvs[i] = buffer[rowOffset + 11 + i];
                }
            }
            int qoffset = kv.getQualifierOffset();
            int hq = (buffer[qoffset] & 0xFF);
            if (hq < 16) {
                int qualifier = (hq << 8) + (buffer[qoffset + 1] & 0xFF);
                fdData[qualifier] = kv.getValue();
            } else {
                extfdData = kv.getValue();
            }
        }
        int[] sizes = new int[16];
        short[] offsets = new short[4096];
        if (extfdData != null) {
            for (int i = 0; i < 16; i++) {
                sizes[i] = extfdData[i] & 0xFF;
            }
            int pos = 16;
            for (int i = 0; i < 16; i++) {
                int size = sizes[i];
                while (size > 0) {
                    int offset = extfdData[pos] & 0xFF;
                    int offsetIdx = (i << 8) + offset;
                    if (fdData[offsetIdx] == null) {
                        offsets[offsetIdx] = (short) (pos + 1);
                        fdData[offsetIdx] = extfdData;
                    }
                    pos = pos + 2 + 8;
                    size--;
                }
            }
        }
        for (int i = 4095; i >= 0; i--) {
            byte[] item = fdData[i];
            long dataPointTimestamp = baseTime + i * 1000;
            if (dataPointTimestamp < queryTimeRange.startTime || dataPointTimestamp >= queryTimeRange.endTime) {
                continue;
            }
            int start = offsets[i];
            if (item != null) {
                double value = Double.longBitsToDouble(Bytes.toLong(item,start+1,8));
                downsampleStream.downSample(nvs, dataPointTimestamp, value);
            }
        }
    }
}
