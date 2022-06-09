package com.laoxin.mq.broker.service;

import java.util.List;

public interface AllocateConsumerStrategy{

    Consumer allocate(List<Consumer> consumers);

    String getName();
}
