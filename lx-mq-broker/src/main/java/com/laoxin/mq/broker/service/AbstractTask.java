package com.laoxin.mq.broker.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class AbstractTask implements Task,Runnable{

    private volatile AtomicBoolean running;
    protected volatile AtomicBoolean pause;

    public AbstractTask(){
        this.running = new AtomicBoolean(false);
        this.pause = new AtomicBoolean(false);
    }

    @Override
    public boolean pause() {
        boolean result = pause.compareAndSet(false,true);
        log.warn("[{}] paused, result={}",getTaskName(),result);
        return result;
    }

    @Override
    public boolean cancelPause() {
        boolean result = pause.compareAndSet(true,false);
        log.warn("[{}] pause canceled, result={}",getTaskName(),result);
        return result;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean taskCanDo() {
        if(isRunning()){
            return false;
        }
        if(pause.get()){
            log.info("[{}] has been paused",getTaskName());
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        if(!taskCanDo()){
            return ;
        }
        if (!running.compareAndSet(false, true)) {
            if(log.isDebugEnabled()){
                log.debug("[{}] is running",getTaskName());
            }
            return;
        }
        try {
            doTask();
        }catch (Exception e){
            completedException(e);
        }finally {
            running.set(false);
        }
    }

    protected abstract String getTaskName();
    protected abstract void completedException(Exception e);
}
