package com.laoxin.mq.broker.service;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractWorker implements Worker{

    protected final TaskScheduler taskScheduler;
    protected ScheduledExecutorService scheduledExecutorService;

    public AbstractWorker(){
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"mq-worker"));
        this.taskScheduler = new ConcurrentTaskScheduler(scheduledExecutorService);
    }

    public AbstractWorker(ScheduledExecutorService scheduledExecutorService){
        this.scheduledExecutorService = scheduledExecutorService;
        this.taskScheduler = new ConcurrentTaskScheduler(scheduledExecutorService);
    }

    public AbstractWorker(TaskScheduler taskScheduler){
        this.taskScheduler = taskScheduler;
    }


    @Override
    public CompletableFuture<Void> close() {
        if(scheduledExecutorService != null){
            scheduledExecutorService.shutdownNow();
        }
        return CompletableFuture.completedFuture(null);
    }
}
