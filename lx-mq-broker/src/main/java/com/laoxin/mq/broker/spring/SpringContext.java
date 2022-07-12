package com.laoxin.mq.broker.spring;

import com.laoxin.mq.broker.mapper.mq.SubscriptionMapper;
import com.laoxin.mq.broker.mapper.mq.TopicMapper;
import com.laoxin.mq.broker.mapper.mq.TraceLogMapper;

public interface SpringContext {

    SubscriptionMapper subscriptionMapper();
    TopicMapper topicMapper();
    TraceLogMapper traceLogMapper();
}
