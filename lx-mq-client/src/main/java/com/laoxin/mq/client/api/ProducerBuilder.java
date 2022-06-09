package com.laoxin.mq.client.api;

import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;
import lombok.NonNull;

import java.util.concurrent.TimeUnit;

public interface ProducerBuilder<T> extends Cloneable{

    Producer<T> create() throws MqClientException;

    ProducerBuilder<T> topic(String topicName);

    ProducerBuilder<T> topicType(TopicType topicType);

    ProducerBuilder<T> producerName(String producerName);

    ProducerBuilder<T> sendTimeout(int sendTimeout, @NonNull TimeUnit unit);

    ProducerBuilder<T> tenantId(long tenantId);
}
