package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.MessageBuilder;
import com.laoxin.mq.client.api.MessageId;
import com.laoxin.mq.client.api.MessageSenderBuilder;
import com.laoxin.mq.client.exception.MqClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MessageSenderBuilderImpl<T> implements MessageSenderBuilder<T> {

    private final ProducerImpl producer;
    private final MessageBuilder messageBuilder;
    private Map<String,String> properties;

    public MessageSenderBuilderImpl(ProducerImpl producer){
        this.producer = producer;
        this.messageBuilder = MessageBuilder.create();
        this.properties = new HashMap<>();
    }

    @Override
    public CompletableFuture<MessageId> sendAsync() {
        messageBuilder.setProperties(properties);
        return producer.sendAsync(messageBuilder.build());
    }

    @Override
    public MessageId send() throws MqClientException {
        messageBuilder.setProperties(properties);
        return producer.send(messageBuilder.build());
    }

    @Override
    public MessageSenderBuilder value(T o) {
        messageBuilder.setContent(o);
        return this;
    }

    @Override
    public MessageSenderBuilder property(String name, String value) {
        properties.put(name,value);
        return this;
    }

    @Override
    public MessageSenderBuilder properties(Map<String,String> properties) {
        this.properties = new HashMap<>(properties);
        return this;
    }
}
