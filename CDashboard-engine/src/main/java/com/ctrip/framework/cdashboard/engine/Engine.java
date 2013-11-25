package com.ctrip.framework.cdashboard.engine;

import com.ctrip.framework.cdashboard.common.config.Configure;
import com.ctrip.framework.cdashboard.common.io.CommandProcessorProvider;
import com.ctrip.framework.cdashboard.engine.command.DefaultCommandProcessor;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * CDashboard engine main
 * User: huang_jie
 * Date: 11/21/13
 * Time: 2:52 PM
 */
public class Engine {
    private ExecutorService fastThreadPool;
    private ExecutorService slowThreadPool;
    private ExecutorService thriftThreadPool;
    private volatile boolean started = false;

    private Engine() {
    }

    private static class EngineHolder {
        public static Engine instance = new Engine();
    }

    public static Engine getInstance() {
        return EngineHolder.instance;
    }

    /**
     * Start CDashboard engine
     *
     * @throws IOException
     */
    public void start() {
        synchronized (this) {
            if (!started) {
                init();
                started = true;
            }
        }
    }

    /**
     * Do engine initial logic
     */
    private void init() {
        CommandProcessorProvider.getInstance().setCommandProcessor(new DefaultCommandProcessor());
        fastThreadPool = new ThreadPoolExecutor(Configure.getInt("fast-thread-pool.corePoolSize", 16),
                Configure.getInt("fast-thread-pool.maximumPoolSize", 32), Configure.getInt("fast-thread-pool.keepAliveTime", 15),
                TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1024));
        slowThreadPool = new ThreadPoolExecutor(Configure.getInt("slow-thread-pool.corePoolSize", 16),
                Configure.getInt("slow-thread-pool.maximumPoolSize", 32), Configure.getInt("slow-thread-pool.keepAliveTime", 15),
                TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1024));
        thriftThreadPool = new ThreadPoolExecutor(Configure.getInt("thrift-thread-pool.corePoolSize", 16),
                Configure.getInt("thrift-thread-pool.maximumPoolSize", 32), Configure.getInt("thrift-thread-pool.keepAliveTime", 15),
                TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1024));
    }

    /**
     * Get lightweight request handle thread pool
     *
     * @return
     */
    public ExecutorService getCommandThreadPool() {
        if (!started) {
            throw new java.lang.IllegalStateException("Engine has not started.");
        }
        return fastThreadPool;
    }

    /**
     * Get high cost request handle thread pool
     *
     * @return
     */
    public ExecutorService getHighCostThreadPool() {
        if (!started) {
            throw new java.lang.IllegalStateException("Engine has not started.");
        }
        return slowThreadPool;
    }

    /**
     * Get thrift request handle thread pool
     *
     * @return
     */
    public ExecutorService getThriftThreadPool() {
        if (!started) {
            throw new java.lang.IllegalStateException("Engine has not started.");
        }
        return thriftThreadPool;
    }
}
