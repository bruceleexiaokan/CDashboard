package com.ctrip.framework.cdashboard.persist.util;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

/**
 * User: huang_jie
 * Date: 7/16/13
 * Time: 9:31 AM
 */
public class TimeRangeSplitUtil {
    public static byte[] getTimeParts(long timestamp) {
        byte[] rt = new byte[5];
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        int month = cal.get(Calendar.MONTH);
        rt[0] = (byte) (month % 2);
        long timePart = Integer.MAX_VALUE - timestamp / (4096000L);
        Bytes.toBytes(rt, 1, timePart, 4);
        return rt;
    }

    public static byte[] getOffset(long timestamp) {
        long offset = (timestamp % 4096000L) / 1000;
        return new byte[]{(byte) ((offset & 0xFF00) >> 8), (byte) (offset & 0xFF)};
    }

    public static int[] getQualifiers(long startTime, long endTime) {
        if (endTime < startTime) {
            throw new IllegalArgumentException("invalid time range.");
        }
        long rowInterval = 4096000L;
        Set<Integer> cols = new TreeSet<Integer>();
        long endOffset = endTime % rowInterval;
        if (endOffset == 0) {
            endOffset = rowInterval - 1;
        }
        long startOffset = startTime % rowInterval;
        long offset = endOffset;
        while (offset > startOffset) {
            int colIndex = (int) ((offset / 1000) & 0xFFFF);
            cols.add(colIndex);
            offset -= 1000;
        }
        int colIndex = (int) ((startOffset / 1000) & 0xFFFF);
        cols.add(colIndex);
        int[] rt = new int[cols.size() + 1];
        int index = 0;
        for (int col : cols) {
            rt[index] = col;
            index++;
        }
        rt[index] = 0xFF00;
        return rt;
    }

}
