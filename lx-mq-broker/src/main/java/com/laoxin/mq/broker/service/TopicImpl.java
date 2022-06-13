package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.impl.MessageIdImpl;
import com.laoxin.mq.client.util.FutureUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class TopicImpl implements Topic{

    private final BrokerService service;
    private final TopicMetaData topicMetaData;

    //主题下的订阅者
    private final ConcurrentHashMap<String, SubscriptionImpl> subscriptions;

    private final Set<Producer> producers;

    private final ReentrantReadWriteLock lock;


    public TopicImpl(TopicMetaData topicMetaData,BrokerService service){
        this.service = service;
        this.topicMetaData = topicMetaData;
        this.subscriptions = new ConcurrentHashMap<>();
        this.producers = new HashSet<>();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public CompletableFuture<Consumer> subscribe(MqServerHandler sh,ConsumerKey consumerKey, SubscriptionMetaData subscriptionMetaData) {

        CompletableFuture<Consumer> consumerFuture = new CompletableFuture<>();

        service.metaStore().storeSubscription(topicMetaData, subscriptionMetaData,(v,e)->{
            if(e == null){
                try {
                    SubscriptionImpl subscription = subscriptions.computeIfAbsent(subscriptionMetaData.getSubscriptionName(),
                            name -> new SubscriptionImpl(this,service, subscriptionMetaData));
                    //TODO refresh?
                    subscription.refreshMetaData(subscriptionMetaData);
                    Consumer consumer = new Consumer(sh,consumerKey,this,subscription);
                    subscription.registerConsumer(consumer);
                    consumerFuture.complete(consumer);
                    return;
                }catch (Exception ex){
                    e = ex;
                }
            }
            log.warn("subscribe consumer error {}");
            consumerFuture.completeExceptionally(e);
        });

        return consumerFuture;
    }

    @Override
    public CompletableFuture<Void> unsubscribe(String subscriptionName) {

        CompletableFuture<Void> future = new CompletableFuture<>();

        SubscriptionKey subscriptionKey = SubscriptionKey
                .builder()
                .tenantId(topicMetaData.getTenantId())
                .topicName(topicMetaData.getTopicName())
                .subscriptionName(subscriptionName)
                .build();

        service.metaStore().removeSubscription(subscriptionKey,(v,e)->{
            if(e == null){
                log.info("removeSubscription success subscriptionKey={}",subscriptionKey);
                subscriptions.remove(subscriptionName);
                future.complete(null);
            }else {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public void registerProducer(Producer producer) {
        lock.readLock().lock();
        try {

            if(!producers.add(producer)){
                log.warn("producer already add on the topic");
                throw new MqServerException("producer already add on the topic");
            }

        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void removeProducer(Producer producer) {

        if(producer.getTopic() != this){
            log.warn("producer is not add in this topic,producer={}",producer);
            throw new MqServerException("producer is not add in this topic");
        }
        lock.writeLock().lock();
        try {
            producers.remove(producer);
        }finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public CompletableFuture<List<Message>> pullMessage(Position position, Map<String, String> subscriptionProperties, Long maxEntryId, int size) {

        ReadMessageRequest request = ReadMessageRequest.builder()
                .position(position)
                .subscriptionProperties(subscriptionProperties)
                .maxEntryId(maxEntryId)
                .readSize(size)
                .build();

        return service.storeManager().getMessageStore(topicMetaData.getTopicType())
                .readMessage(request);
    }

    @Override
    public void publishMessage(String message, PublishCallback callback) {
        try {
            final MessageIdImpl messageId = service.storeManager()
                    .getMessageStore(topicMetaData.getTopicType())
                    .writeMessage(TopicKey.builder()
                                    .topicName(topicMetaData.getTopicName())
                                    .tenantId(topicMetaData.getTenantId())
                                    .build(),
                            message
                    );
            callback.completed(null,messageId.getEntryId());
            //触发消息推送
            triggerPush();
        }catch (Exception e){
            callback.completed(e,0);
        }
    }

    private void triggerPush(){

    }

    @Override
    public ConcurrentHashMap<String, SubscriptionImpl> getSubscriptions() {
        return subscriptions;
    }

    @Override
    public Set<Producer> getProducers() {
        return producers;
    }

    @Override
    public CompletableFuture<Void> close() {
        log.info("topic[{}] closing...",topicMetaData);
        CompletableFuture<Void> closeFuture = new CompletableFuture<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        producers.forEach(producer -> futures.add(producer.disconnect()));
        subscriptions.forEach((s, sub) -> futures.add(sub.disconnect()));

        FutureUtil.waitForAll(futures)
                .thenRun(()->{
                    closeFuture.complete(null);
                }).exceptionally(e->{
                    closeFuture.completeExceptionally(e);
                    return null;
                });
        log.info("topic[{}] closed",topicMetaData);
        return closeFuture;
    }

    TopicMetaData metaData(){
        return topicMetaData;
    }
}
