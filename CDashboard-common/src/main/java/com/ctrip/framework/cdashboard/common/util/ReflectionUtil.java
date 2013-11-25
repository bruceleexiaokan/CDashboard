package com.ctrip.framework.cdashboard.common.util;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * General reflection utility tools
 * User: huang_jie
 * Date: 11/21/13
 * Time: 2:30 PM
 */
public class ReflectionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);

    /**
     * Scan sub class implement from package for super class
     *
     * @param packageName scan package
     * @param superClass  super class
     * @param <T>
     * @return
     */
    public static <T> Set<Class<? extends T>> scanSubTypeFromPackage(String packageName, Class<T> superClass) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(superClass);
    }

    /**
     * Create sub class instance from package for super class
     *
     * @param packageName scan package
     * @param superClass  super class
     * @param <T>
     * @return
     */
    public static <T> Set<T> newInstanceFromPackage(String packageName, Class<T> superClass) {
        Set<Class<? extends T>> classSet = scanSubTypeFromPackage(packageName, superClass);
        Set<T> instanceSet = new HashSet<T>();
        if (classSet == null || classSet.size() <= 0) {
            return instanceSet;
        }
        for (Class<? extends T> tClass : classSet) {
            try {
                instanceSet.add(tClass.newInstance());
            } catch (Exception e) {
                LOGGER.warn("Cannot new an instance for " + tClass.getName(), e);
            }
        }
        return instanceSet;
    }
}
