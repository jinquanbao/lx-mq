package com.laoxin.mq.broker.trace;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class TraceLogContextImpl implements TraceLogContext{

    private final int batchFlushSize;
    private final int queueSize;
    private final AtomicLong discardCount;
    private final ArrayBlockingQueue<TraceLogInfo> traceLogQueue;
    private final Thread worker;
    private final TraceLogFlush traceLogFlush;

    public TraceLogContextImpl(TraceLogFlush flush){
        this.batchFlushSize = 50;
        this.queueSize = 2048;
        this.discardCount = new AtomicLong(0L);
        this.traceLogQueue = new ArrayBlockingQueue<>(queueSize);
        this.worker = new Thread(this::flush,"trace-log");
        this.traceLogFlush = flush;
    }

    @Override
    public void start() {
        this.worker.start();
    }

    @Override
    public boolean log(TraceLogInfo info) {
        boolean result = traceLogQueue.offer(info);
        if (!result) {
            log.info("traceLogQueue is full,discardCount={}",discardCount.incrementAndGet());
        }
        return result;
    }

    private void flush(){
        List<TraceLogInfo> list = new ArrayList<>(batchFlushSize);
        while (!Thread.currentThread().isInterrupted()){
            try {

                TraceLogInfo info = traceLogQueue.poll();
                if(info == null){
                    if(list.isEmpty()){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }else {
                        traceLogFlush.flush(list);
                        list.clear();
                    }
                }else {
                    list.add(info);
                    if(list.size()>=batchFlushSize){
                        traceLogFlush.flush(list);
                        list.clear();
                    }
                }
            }catch (Exception e){
                log.error("trace log flush error {}",e.getMessage());
            }
        }
        log.warn("flush work interrupted");
    }

    @Override
    public void close() {
        log.info("trace log close...");
        worker.interrupt();
        List<TraceLogInfo> list = new ArrayList<>(traceLogQueue.size());
        while (!traceLogQueue.isEmpty()){
            list.add(traceLogQueue.poll());
        }
        traceLogFlush.flush(list);
        log.info("trace log closed");
    }
}
