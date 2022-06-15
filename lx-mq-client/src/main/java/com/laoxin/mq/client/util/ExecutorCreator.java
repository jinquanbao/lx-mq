package com.laoxin.mq.client.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

@Slf4j
public class ExecutorCreator {

    public static ExecutorService createDiscardExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, String threadNamePrefix){
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();
        threadFactory.setThreadNamePrefix(threadNamePrefix);
        ExecutorService executorService =
                new ThreadPoolExecutor(corePoolSize,maxPoolSize,1,TimeUnit.MINUTES,workQueue,threadFactory,(r,pool)->{
                    log.warn("队列已满,丢弃任务");
                });
        return executorService;
    }
}
