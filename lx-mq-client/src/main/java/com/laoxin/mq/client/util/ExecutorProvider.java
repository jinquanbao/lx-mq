package com.laoxin.mq.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorProvider {

    private final int numThreads;
    private final List<ExecutorService> executors;
    private final AtomicInteger currentThread = new AtomicInteger(0);

    public ExecutorProvider(int numThreads, String threadNamePrefix) {
        this.numThreads = numThreads;
        executors = new ArrayList<>(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executors.add(Executors.newSingleThreadExecutor(r->new Thread(r,threadNamePrefix)));
        }
    }

    public ExecutorService getExecutor() {
        return executors.get((currentThread.getAndIncrement() & Integer.MAX_VALUE) % numThreads);
    }

    public void shutdownNow() {
        executors.forEach(executor -> executor.shutdownNow());
    }

}
