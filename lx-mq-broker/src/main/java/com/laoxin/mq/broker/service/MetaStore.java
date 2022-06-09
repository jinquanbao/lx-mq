package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;

public interface MetaStore {

    default void start() {
    }

    boolean storageType(String storageType);

    void storeTopic(TopicMetaData topicMetaData,MetaStoreCallback callback);

    void storeSubscription(TopicMetaData topicMetaData,SubscriptionMetaData subscriptionMetaData,MetaStoreCallback callback);

    void removeSubscription(SubscriptionKey subscriptionKey,MetaStoreCallback callback);

    void removeSubscriptionConsumer(SubscriptionKey subscriptionKey, SubscriptionConsumer subscriptionConsumer, MetaStoreCallback callback);


}
