package com.laoxin.mq.client.api;

import com.laoxin.mq.client.exception.MqClientException;

import java.io.Closeable;

public interface MqClient extends Closeable {

    static ClientBuilder builder(){
        return new ClientBuilder();
    }

    ProducerBuilder<String> newProducer();

    <T> ProducerBuilder<T> newProducer(Class<T> pojo);

    ConsumerBuilder<String> newConsumer();

    <T> ConsumerBuilder<T> newConsumer(Class<T> pojo);

    @Override
    void close()throws MqClientException;
}
