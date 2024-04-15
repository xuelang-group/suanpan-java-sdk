package com.xuelang.suanpan.common.pool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

public class ThreadPool {
    private static final ThreadFactory namedThreadPoolFactory = new ThreadFactoryBuilder().setNameFormat("suanpan-pool-%d").build();
    private static volatile ThreadPoolExecutor threadPoolExecutor = null;
    private static final int cpus = Math.max(Runtime.getRuntime().availableProcessors(), 2);

    private static final ThreadFactory singleThreadPoolFactory = new ThreadFactoryBuilder().setNameFormat("single-pool-%d").build();
    private static volatile ThreadPoolExecutor singleThreadPoolExecutor = null;


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

    public static ThreadPoolExecutor singlePool() {
        if (singleThreadPoolExecutor == null) {
            synchronized (singleThreadPoolFactory) {
                if (singleThreadPoolExecutor == null) {
                    singleThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 0,
                            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1), singleThreadPoolFactory,
                            new ThreadPoolExecutor.DiscardPolicy());
                }
            }
        }

        return singleThreadPoolExecutor;
    }

}
