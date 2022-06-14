package com.laoxin.mq.client.api;

import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface ConsumerBuilder<T> {

    Consumer subscribe() throws MqClientException;

    ConsumerBuilder<T> consumerName(String consumerName);

    ConsumerBuilder<T> topic(String topic);

    ConsumerBuilder<T> topicType(TopicType topicType);

    ConsumerBuilder<T> tenantId(long tenantId);

    ConsumerBuilder<T> subscriptionName(String subscription);

    ConsumerBuilder<T> ackTimeOut(long ackTimeout, TimeUnit timeUnit);

    ConsumerBuilder<T> dependencyOnSubscription(String subscription);

    ConsumerBuilder<T> subscriptionType(SubscriptionType subscriptionType);

    //设置了监听器就是push模式，不设置就是pull模式
    ConsumerBuilder<T> messageListener(MessageListener<T> listener);

    //仅支持客户端过滤
    ConsumerBuilder<T> filterExpression(String filterExpression);

    //仅支持客户端过滤
    ConsumerBuilder<T> messageFilter(MessageFilter<T> filter);

    ConsumerBuilder<T> subscriptionProperty(String name, String value);

    //消息标签过滤tag设置，服务端过滤
    ConsumerBuilder<T> subscriptionProperties(Map<String,String> subscriptionProperties);




}