package com.ctrip.framework.cdashboard.engine;

import com.ctrip.framework.cdashboard.common.io.InitListener;

/**
 * User: huang_jie
 * Date: 11/21/13
 * Time: 1:07 PM
 */
public class EngineInit implements InitListener {
    /**
     * Initial CDashboard Engine
     */
    @Override
    public void init() {
        Engine.getInstance().start();
    }
}
