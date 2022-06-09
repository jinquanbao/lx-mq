package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.util.FutureUtil;

import java.util.Map;
import java.util.concurrent.*;

public class MessagePushWorker implements Worker{

    private final Map<TopicKey, CompletableFuture<Topic>> topics;
    private final ScheduledExecutorService scheduledExecutorService;

    public MessagePushWorker(Map<TopicKey, CompletableFuture<Topic>> topics){
        this.topics = topics;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"message-push-scheduled"));
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(this::triggerPush,5000,5000,TimeUnit.MILLISECONDS);
    }

    private void triggerPush(){
        topics.forEach((k,future)->{
            if(FutureUtil.futureSuccess(future)){
                Topic topic = future.getNow(null);
                ConcurrentHashMap<String, SubscriptionImpl> subscriptions = topic.getSubscriptions();
                subscriptions.forEach((subName,subscription)->{
                    subscription.triggerPush();
                });
            }
        });
    }

    @Override
    public CompletableFuture<Void> close() {
        scheduledExecutorService.shutdownNow();
        return CompletableFuture.completedFuture(null);
    }
}
