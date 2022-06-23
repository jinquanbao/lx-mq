package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.broker.position.PositionKey;
import com.laoxin.mq.client.api.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Subscription {

    void registerConsumer(Consumer consumer);

    boolean removeConsumer(Consumer consumer);

    List<Consumer> getConsumers();

    CompletableFuture<List<Message>> pullMessage(Position position, int size);

    void triggerPush();

    @Deprecated
    void ack(Position position);

    void ack(PositionKey positionKey,List<Long> entryIds);

    boolean seek(long entryId);

    CompletableFuture<Void> close();

    CompletableFuture<Void> disconnect();

    boolean isOnline();

    Consumer allocateConsumer();

    void refreshMetaData(SubscriptionMetaData subscriptionMetaData);

}
