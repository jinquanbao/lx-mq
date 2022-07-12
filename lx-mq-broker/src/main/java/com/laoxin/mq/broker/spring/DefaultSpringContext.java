package com.laoxin.mq.broker.spring;

import com.laoxin.mq.broker.mapper.mq.SubscriptionMapper;
import com.laoxin.mq.broker.mapper.mq.TopicMapper;
import com.laoxin.mq.broker.mapper.mq.TraceLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultSpringContext implements SpringContext{

    private final SubscriptionMapper subscriptionMapper;
    private final TopicMapper topicMapper;
    private final TraceLogMapper traceLogMapper;


    @Override
    public SubscriptionMapper subscriptionMapper() {
        return subscriptionMapper;
    }

    @Override
    public TopicMapper topicMapper() {
        return topicMapper;
    }

    @Override
    public TraceLogMapper traceLogMapper() {
        return traceLogMapper;
    }
}
