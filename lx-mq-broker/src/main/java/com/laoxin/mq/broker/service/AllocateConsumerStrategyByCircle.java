package com.laoxin.mq.broker.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AllocateConsumerStrategyByCircle implements AllocateConsumerStrategy{

    private AtomicInteger allocateCount = new AtomicInteger();

    @Override
    public Consumer allocate(List<Consumer> consumers) {
        if(consumers == null || consumers.size() ==0){
            return null;
        }
        if(consumers.size() == 1){
            return consumers.get(0);
        }
        int index = allocateCount.getAndIncrement() % consumers.size();
        if(allocateCount.get() % consumers.size() == 0){
            allocateCount.compareAndSet(allocateCount.get(),0);
        }
        return consumers.get(index);
    }

    @Override
    public String getName() {
        return "circle";
    }
}
