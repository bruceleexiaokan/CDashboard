package com.ctrip.framework.cdashboard.persist.util;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * User: huang_jie
 * Date: 11/22/13
 * Time: 4:21 PM
 */
public class HBaseClientUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HBaseClientUtil.class);

    public static void closeHTable(HTableInterface table) {
        if (table != null) {
            try {
                table.close();
            } catch (IOException e) {
                LOGGER.warn("Close HBase table error.", e);
            }
        }
    }

    public static void closeResultScanner(ResultScanner resultScanner) {
        if (resultScanner != null) {
            resultScanner.close();
        }
    }

    public static void closeResource(HTableInterface table, ResultScanner resultScanner) {
        closeHTable(table);
        closeResultScanner(resultScanner);
    }
}
