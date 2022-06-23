package com.laoxin.mq.client.api;

import com.laoxin.mq.client.exception.MqClientException;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Consumer<T> extends Closeable {

    String getTopic();

    String getSubscription();

    void unsubscribe() throws MqClientException;

    List<Message<T>> pull() throws MqClientException;

    List<Message<T>> pull(MessageId latestId, int batchSize) throws MqClientException;

    void ack(Message msg)throws MqClientException;

    void ack(List<Message> msgs)throws MqClientException;

    CompletableFuture<Void> ackAsync(Message msg);

    CompletableFuture<Void> ackAsync(List<Message> msgs);

    //重置持久化位移消费偏移量,重置的时候最好不是消费中，否则会导致消费中的消息ack后又覆盖了seek后的偏移量
    void seek(MessageId msgId)throws MqClientException;

    @Override
    void close() throws MqClientException;

    long consumerId();
}
