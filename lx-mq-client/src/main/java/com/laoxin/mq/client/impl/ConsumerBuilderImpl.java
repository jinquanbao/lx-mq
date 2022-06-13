package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.Consumer;
import com.laoxin.mq.client.api.ConsumerBuilder;
import com.laoxin.mq.client.api.MessageFilter;
import com.laoxin.mq.client.api.MessageListener;
import com.laoxin.mq.client.conf.ConsumerConfigurationData;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConsumerBuilderImpl<T> implements ConsumerBuilder<T> {

    private final ConsumerConfigurationData conf;

    private final MqClientImpl client;

    private final Class<T> pojo;

    public ConsumerBuilderImpl(MqClientImpl client,Class<T> pojo){
        this(client,new ConsumerConfigurationData(),pojo);
    }

    private ConsumerBuilderImpl(MqClientImpl client,ConsumerConfigurationData conf,Class<T> pojo){
        this.conf = conf;
        this.client = client;
        this.pojo = pojo;
    }

    @Override
    public Consumer subscribe() throws MqClientException {
        if(!StringUtils.hasText(conf.getTopic())){
            throw new IllegalArgumentException("Topic name must be set on the consumer builder.");
        }
        if(!StringUtils.hasText(conf.getSubscriptionName())){
            throw new IllegalArgumentException("Subscription name must be set on the consumer builder.");
        }
        return client.subscribe(conf,pojo);
    }

    @Override
    public ConsumerBuilder<T> consumerName(String consumerName) {
        conf.setConsumerName(consumerName);
        return this;
    }

    @Override
    public ConsumerBuilder<T> topic(String topic) {
        conf.setTopic(topic);
        return this;
    }

    @Override
    public ConsumerBuilder<T> topicType(TopicType topicType) {
        conf.setTopicType(topicType);
        return this;
    }

    @Override
    public ConsumerBuilder<T> tenantId(long tenantId) {
        conf.setTenantId(tenantId);
        return this;
    }


    @Override
    public ConsumerBuilder<T> subscriptionName(String subscription) {
        conf.setSubscriptionName(subscription);
        return this;
    }

    @Override
    public ConsumerBuilder<T> ackTimeOut(long ackTimeout, TimeUnit timeUnit) {
        if(timeUnit == null){
            throw new NullPointerException("timeUnit is marked non-null but is null");
        }
        conf.setAckTimeoutMillis(timeUnit.toMillis(ackTimeout));
        return this;
    }

    @Override
    public ConsumerBuilder<T> dependencyOnSubscription(String subscription) {
        conf.setDependencyOnSubscription(subscription);
        return null;
    }

    @Override
    public ConsumerBuilder<T> subscriptionType(SubscriptionType subscriptionType) {
        conf.setSubscriptionType(subscriptionType);
        return this;
    }

    @Override
    public ConsumerBuilder<T> messageListener(MessageListener<T> listener) {
        conf.setMessageListener(listener);
        return this;
    }

    //@Override
    public ConsumerBuilder<T> filterExpression(String filterExpression) {
        conf.setFilterExpression(filterExpression);
        return this;
    }

    @Override
    public ConsumerBuilder<T> messageFilter(MessageFilter<T> filter) {
        conf.setFilter(filter);
        return this;
    }

    @Override
    public ConsumerBuilder<T> subscriptionProperty(String name, String value) {
        Map subscriptionProperties = conf.getSubscriptionProperties();
        if(subscriptionProperties == null){
            subscriptionProperties = new HashMap();
            conf.setSubscriptionProperties(subscriptionProperties);
        }
        subscriptionProperties.put(name,value);
        return this;
    }

    @Override
    public ConsumerBuilder<T> subscriptionProperties(Map<String, String> subscriptionProperties) {
        conf.setSubscriptionProperties(subscriptionProperties);
        return this;
    }


}
