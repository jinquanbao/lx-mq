package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.Producer;
import com.laoxin.mq.client.api.ProducerBuilder;
import com.laoxin.mq.client.conf.ProducerConfigurationData;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

public class ProducerBuilderImpl<T> implements ProducerBuilder<T> {

    private final ProducerConfigurationData conf;

    private final MqClientImpl client;

    private final Class<T> pojo;

    public ProducerBuilderImpl(MqClientImpl client,  Class<T> pojo) {
        this(client,new ProducerConfigurationData(),pojo);
    }

    private ProducerBuilderImpl(MqClientImpl client, ProducerConfigurationData conf, Class<T> pojo) {
        this.client = client;
        this.conf = conf;
        this.pojo = pojo;
    }

    @Override
    public Producer<T> create() throws MqClientException {
        if(!StringUtils.hasText(conf.getTopic())){
            throw new IllegalArgumentException("Topic name must be set on the produce builder.");
        }
        if(conf.getTenantId()<1){
            throw new IllegalArgumentException("tenantId be set on the produce builder.");
        }
        return client.createProducer(conf,pojo);
    }

    @Override
    public ProducerBuilder<T> topic(String topicName) {
        conf.setTopic(topicName);
        return this;
    }

    @Override
    public ProducerBuilder<T> topicType(TopicType topicType) {
        conf.setTopicType(topicType);
        return this;
    }

    @Override
    public ProducerBuilder<T> producerName(String producerName) {
        conf.setProduceName(producerName);
        return this;
    }

    @Override
    public ProducerBuilder<T> sendTimeout(int sendTimeout, TimeUnit unit) {
        if(unit == null){
            throw new NullPointerException("unit is marked non-null but is null");
        }
        conf.setSendTimeoutMs(unit.toMillis(sendTimeout));
        return this;
    }

    @Override
    public ProducerBuilder<T> tenantId(long tenantId) {
        conf.setTenantId(tenantId);
        return this;
    }
}
