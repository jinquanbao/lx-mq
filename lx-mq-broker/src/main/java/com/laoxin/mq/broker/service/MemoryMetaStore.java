package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;
import com.laoxin.mq.broker.enums.StoreType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryMetaStore implements MetaStore{

    private Map<TopicKey, TopicMetaData> topicMeta = new ConcurrentHashMap<>();
    private Map<SubscriptionKey, SubscriptionMetaData> subscriptionMetaDataMap = new ConcurrentHashMap<>();
    private Map<SubscriptionKey, Set<SubscriptionConsumer>> subscriptionConsumerMap = new ConcurrentHashMap<>();


    @Override
    public boolean storageType(String storageType) {
        return StoreType.memory.name().equalsIgnoreCase(storageType);
    }

    @Override
    public void storeTopic(TopicMetaData metaData, MetaStoreCallback callback) {
        topicMeta.computeIfAbsent(TopicKey.builder()
                .tenantId(metaData.getTenantId())
                .topicName(metaData.getTopicName()).build(),topicKey -> metaData);
        callback.complete(metaData,null);
    }

    @Override
    public void storeSubscription(TopicMetaData topicMetaData ,SubscriptionMetaData subscriptionMetaData, MetaStoreCallback callback) {

        final SubscriptionKey subscriptionKey = SubscriptionKey
                .builder()
                .tenantId(topicMetaData.getTenantId())
                .topicName(topicMetaData.getTopicName())
                .subscriptionName(subscriptionMetaData.getSubscriptionName())
                .build();

        //update
        subscriptionMetaDataMap.compute(subscriptionKey,(k,v)->subscriptionMetaData);

        //add
        subscriptionConsumerMap.computeIfAbsent(subscriptionKey, k -> new HashSet<>())
                .add(subscriptionMetaData.getConsumer());

        callback.complete(subscriptionMetaData,null);
    }

    @Override
    public void removeSubscription(SubscriptionKey subscriptionKey, MetaStoreCallback callback) {
        subscriptionConsumerMap.remove(subscriptionKey);
        callback.complete(subscriptionMetaDataMap.remove(subscriptionKey),null);
    }

    @Override
    public void removeSubscriptionConsumer(SubscriptionKey subscriptionKey, SubscriptionConsumer subscriptionConsumer, MetaStoreCallback callback) {
        final Set<SubscriptionConsumer> consumers = subscriptionConsumerMap.get(subscriptionKey);
        if(consumers != null){
            consumers.remove(subscriptionConsumer);
        }
        callback.complete(subscriptionConsumer,null);
    }
}
