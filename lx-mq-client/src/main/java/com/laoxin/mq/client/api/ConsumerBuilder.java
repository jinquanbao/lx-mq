package com.laoxin.mq.client.api;

import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;

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

    ConsumerBuilder<T> messageListener(MessageListener<T> listener);

    ConsumerBuilder<T> filterExpression(String filterExpression);

    ConsumerBuilder<T> messageFilter(MessageFilter<T> filter);




}
