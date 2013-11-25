package com.ctrip.framework.cdashboard.persist.util;

/**
 * General byte array utility tool
 * User: huang_jie
 * Date: 11/22/13
 * Time: 1:09 PM
 */
public class Bytes extends org.apache.hadoop.hbase.util.Bytes {
    public static void toBytes(byte[] bytes, int offset, long value, int len) {
        long tmp = value;
        for (int i = 7; i >= 8 - len; i--) {
            bytes[offset + (i - (8 - len))] = (byte) (tmp & (0xFFL));
            tmp = tmp >> 8;
        }
    }

    public static long toLong(byte[] value, int start, int len) {
        long rt = 0;
        for (int i = start; i < start + len; i++) {
            int add = value[i] & (0xFF);
            rt = rt << 8;
            rt += add;
        }
        return rt;
    }
}
