package com.ctrip.framework.cdashboard.common.config;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General configuration utility tools based on commons configuration framework lib
 * User: huang_jie
 * Date: 11/21/13
 * Time: 3:16 PM
 */
public class Configure {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configure.class);
    private DefaultConfigurationBuilder builder = null;
    private Configuration config;
    private static Configure instance = new Configure();

    private Configure() {
        try {
            builder = new DefaultConfigurationBuilder("config.xml");
            config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            LOGGER.error("Load configuration error, please check the configure. ", e);
        }
    }

    private static class ConfigureHolder {
        private static Configure instance = new Configure();
    }

    private static Configure getInstance() {
        return ConfigureHolder.instance;
    }

    /**
     * Get int value from configuration file
     *
     * @param key
     * @return
     */
    public static int getInt(String key) {
        return instance.config.getInt(key);
    }

    /**
     * Get int value from configuration file, if not exist, use default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(String key, int defaultValue) {
        return instance.config.getInt(key, defaultValue);
    }

    /**
     * Get string value from configuration file
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        return instance.config.getString(key);
    }

    /**
     * Get string value from configuration file, if not exist, use default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(String key, String defaultValue) {
        return instance.config.getString(key, defaultValue);
    }

    /**
     * Get string value from configuration file based on XPath
     *
     * @param xpath
     * @return
     */
    public static String getStringByXPath(String xpath) {
        CombinedConfiguration config;
        try {
            config = instance.builder.getConfiguration(true);
            config.setExpressionEngine(new XPathExpressionEngine());
            return config.getString(xpath);
        } catch (ConfigurationException e) {
            LOGGER.error("Get configuration value by XPath error: ", e);
        }
        return null;
    }
}
