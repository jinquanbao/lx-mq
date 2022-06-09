package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.util.FutureUtil;

import java.util.Map;
import java.util.concurrent.*;

public class MessageReadWorker implements Worker{

    private final Map<TopicKey, CompletableFuture<Topic>> topics;
    private final ScheduledExecutorService scheduledExecutorService;

    public MessageReadWorker(Map<TopicKey, CompletableFuture<Topic>> topics){
        this.topics = topics;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"message-read-scheduled"));
    }

    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(this::triggerReadMessage,10,10,TimeUnit.MILLISECONDS);
    }

    private void triggerReadMessage(){
        topics.forEach((k,future)->{
            if(FutureUtil.futureSuccess(future)){
                Topic topic = future.getNow(null);
                ConcurrentHashMap<String, SubscriptionImpl> subscriptions = topic.getSubscriptions();
                subscriptions.forEach((subName,subscription)->{
                    subscription.triggerReadMessage();
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
