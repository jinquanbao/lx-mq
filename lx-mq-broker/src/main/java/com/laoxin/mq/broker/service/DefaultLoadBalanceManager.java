package com.laoxin.mq.broker.service;

public class DefaultLoadBalanceManager implements LoadBalanceManager{
    @Override
    public AllocateConsumerStrategy consumerLoadBalanceStrategy() {
        return new AllocateConsumerStrategyByCircle();
    }
}
