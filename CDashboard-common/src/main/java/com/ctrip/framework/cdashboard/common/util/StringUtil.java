package com.ctrip.framework.cdashboard.common.util;

/**
 * General string utility tool
 * User: huang_jie
 * Date: 11/22/13
 * Time: 10:51 AM
 */
public class StringUtil {
    /**
     * Parse interval string to long
     *
     * @param interval
     * @return
     */
    public static long parseInterval(String interval) {
        long rt;
        String check = interval.trim();
        if (check.endsWith("s")) {
            rt = Long.parseLong(interval.substring(0, check.length() - 1)) * 1000;
        } else if (check.endsWith("m")) {
            rt = Long.parseLong(interval.substring(0, check.length() - 1)) * 60000;
        } else if (check.endsWith("h")) {
            rt = Long.parseLong(interval.substring(0, check.length() - 1)) * 3600000;
        } else if (check.endsWith("d")) {
            rt = Long.parseLong(interval.substring(0, check.length() - 1)) * 86400000;
        } else if (check.endsWith("M")) {
            rt = Long.parseLong(interval.substring(0, check.length() - 1)) * 1000 * 60 * 60 * 24 * 30;
        } else {
            rt = Long.parseLong(check) * 1000;
        }
        return rt;
    }

    /**
     * Check tag value whether match in tag value array
     * @param tagValue
     * @param tagValues
     * @param startWith
     * @param endWith
     * @return
     */
    public static boolean tagValueMatch(String tagValue, String[] tagValues, boolean startWith, boolean endWith) {
        int index = tagValue.indexOf(tagValues[0]);
        if (startWith && index < 0) {
            return false;
        }
        int lastIndex = tagValue.lastIndexOf(tagValues[tagValues.length - 1]);
        if (!endWith && !tagValue.endsWith(tagValues[tagValues.length - 1])) {
            return false;
        }
        for (String value : tagValues) {
            int id = tagValue.indexOf(value, index);
            if (id < 0 || id > lastIndex) {
                return false;
            }
        }
        return true;
    }
}
