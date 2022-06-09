package com.laoxin.mq.client.api;

import com.laoxin.mq.client.exception.MqClientException;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Producer<T> extends Closeable {

    String getTopic();

    String getProducerName();

    MessageId send(T msg) throws MqClientException;

    CompletableFuture<MessageId> sendAsync(T msg);

    MessageId send(List<T> batchMsg) throws MqClientException;

    CompletableFuture<MessageId> sendAsync(List<T> batchMsg);

    MessageId send(Message<T> message) throws MqClientException;

    CompletableFuture<MessageId> sendAsync(Message<T> message);

    void close() throws MqClientException;

    CompletableFuture<Void> closeAsync();
}
