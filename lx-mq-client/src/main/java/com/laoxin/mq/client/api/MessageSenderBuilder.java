package com.laoxin.mq.client.api;

import com.laoxin.mq.client.exception.MqClientException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface MessageSenderBuilder<T> {

    CompletableFuture<MessageId> sendAsync();

    MessageId send() throws MqClientException;

    MessageSenderBuilder value(T t);

    MessageSenderBuilder<T> property(String name, String value);

    MessageSenderBuilder<T> properties(Map<String, String> properties);


}
