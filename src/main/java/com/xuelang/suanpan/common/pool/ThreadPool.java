package com.xuelang.suanpan.common.pool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

public class ThreadPool {
    private static final ThreadFactory namedThreadPoolFactory = new ThreadFactoryBuilder().setNameFormat("suanpan-pool-%d").build();
    private static volatile ThreadPoolExecutor threadPoolExecutor = null;
    private static final int cpus = Math.max(Runtime.getRuntime().availableProcessors(), 2);


    public static ThreadPoolExecutor pool() {
        if (threadPoolExecutor == null) {
            synchronized (namedThreadPoolFactory) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(cpus, cpus * 2, 0,
                            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100), namedThreadPoolFactory);
                }
            }
        }

        return threadPoolExecutor;
    }

}
