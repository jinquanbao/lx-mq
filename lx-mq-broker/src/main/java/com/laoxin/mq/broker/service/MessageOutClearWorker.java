package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.util.FutureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.config.CronTask;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Slf4j
public class MessageOutClearWorker extends AbstractWorker{

    private final Map<TopicKey, CompletableFuture<Topic>> topics;

    private final CronTask task;

    public MessageOutClearWorker(Map<TopicKey, CompletableFuture<Topic>> topics){
        super(Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"msg-out-clear")));
        this.topics = topics;
        this.task = new CronTask(this::triggerClear,"0 0 3 * * ?");
    }

    @Override
    public void start() {
        taskScheduler.schedule(task.getRunnable(),task.getTrigger());
    }

    private void triggerClear(){
        log.info("MessageOut expired Clear...");
        topics.forEach((k,future)->{
            if(FutureUtil.futureSuccess(future)){
                Topic topic = future.getNow(null);
                ConcurrentHashMap<String, SubscriptionImpl> subscriptions = topic.getSubscriptions();
                subscriptions.forEach((subName,subscription)->{
                    subscription.consumerInterceptContext().clear();
                });
            }
        });
        log.info("MessageOut expired Clear end");
    }

}
