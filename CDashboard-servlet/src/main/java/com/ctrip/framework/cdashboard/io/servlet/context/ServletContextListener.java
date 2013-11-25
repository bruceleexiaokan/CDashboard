package com.ctrip.framework.cdashboard.io.servlet.context;


import com.ctrip.framework.cdashboard.common.io.InitListener;
import com.ctrip.framework.cdashboard.common.util.ReflectionUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.util.Set;

/**
 * Implement servlet context listener, do some initial logic when servlet container startup
 * User: huang_jie
 * Date: 11/21/13
 * Time: 2:22 PM
 */
@WebListener
public class ServletContextListener implements javax.servlet.ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Set<InitListener> initListeners = ReflectionUtil.newInstanceFromPackage("com.ctrip.framework.cdashboard.engine", InitListener.class);
        for (InitListener initListener : initListeners) {
            initListener.init();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
