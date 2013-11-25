package com.ctrip.framework.cdashboard.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * General file utility tool
 * User: huang_jie
 * Date: 11/22/13
 * Time: 12:58 PM
 */
public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    /**
     * Create file in parent path, if file not exists
     *
     * @param path
     * @param filename
     * @return
     */
    public static boolean createNotExists(String path, String filename) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }

            String filePath = path + File.separator + filename;
            File file1 = new File(filePath);
            if (!file1.exists()) {
                file1.createNewFile();
                if (!file1.exists()) {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            LOGGER.warn("Create file error: ", e);
        }
        return false;
    }

}
