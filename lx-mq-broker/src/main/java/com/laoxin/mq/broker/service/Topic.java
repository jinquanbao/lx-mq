package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.client.api.Message;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public interface Topic {

    public interface PublishCallback {
        void completed(Exception e, long entryId);
    }

    CompletableFuture<Consumer> subscribe(MqServerHandler sh,ConsumerKey consumerKey ,SubscriptionMetaData subscriptionMetaData);

    CompletableFuture<Void> unsubscribe(String subscriptionName);

    void registerProducer(Producer producer);

    void removeProducer(Producer producer);

    CompletableFuture<List<Message>> pullMessage(Position position, Long maxEntryId, int size);

    void publishMessage(String message,PublishCallback callback);

    ConcurrentHashMap<String, SubscriptionImpl> getSubscriptions();

    Set<Producer> getProducers();

    CompletableFuture<Void> close();
}
