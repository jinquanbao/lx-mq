package com.laoxin.mq.broker.service;

public interface LoadBalanceManager {

    AllocateConsumerStrategy consumerLoadBalanceStrategy();
}
