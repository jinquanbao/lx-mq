package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.broker.position.PositionKey;
import com.laoxin.mq.broker.stats.ConsumerStatsImpl;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.command.CommandAck;
import com.laoxin.mq.client.command.CommandPull;
import com.laoxin.mq.client.command.CommandSeek;
import com.laoxin.mq.client.command.Commands;
import com.laoxin.mq.client.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Consumer {

    private final MqServerHandler sh;

    private final SubscriptionImpl subscription;

    private ConsumerKey consumerKey;
    //private final String consumerName;
    private String subscriptionType;
    private SubscriptionConsumer subscriptionConsumer;

    private final ConsumerStatsImpl stats;

    private final boolean enableMonitor;

    public Consumer(MqServerHandler sh,ConsumerKey consumerKey,TopicImpl topic, SubscriptionImpl subscription){
        this.consumerKey = consumerKey;
        this.subscriptionConsumer = subscription.metaData().getConsumer();
        //this.consumerName = subscriptionConsumer.getConsumerName();
        this.subscription = subscription;
        this.subscriptionType = subscription.metaData().getSubscriptionType();
        this.sh = sh;
        this.enableMonitor = topic.brokerConf().isEnableMonitor();
        this.stats = new ConsumerStatsImpl(consumerKey.getTenantId(),subscriptionConsumer.getConsumerName());
        afterPropertiesSet();
    }

    private void afterPropertiesSet(){
        stats.address = subscriptionConsumer.getAddress();
        stats.connectedTimestamp = System.currentTimeMillis();
    }


    public CompletableFuture<List<Message>> pull(CommandPull pull){

        PositionKey positionKey = PositionKey.builder()
                .tenantId(pull.getTenantId())
                .subscription(pull.getSubscription())
                .topic(pull.getTopic())
                .build();

        final Position position = Position.builder()
                .positionKey(positionKey)
                .entryId(pull.getEntryId())
                .build();

        return subscription.pullCacheMessage(position,pull.getSize());
    }

    public void pullSendSuccess(List<Message> messages){
        updateStats(messages);
    }

    private void updateStats(List<Message> messages){
        if(enableMonitor && messages != null && !messages.isEmpty()){
            stats.lastMsgOutTimestamp = System.currentTimeMillis();
            stats.setLastMsgOutPosition(messages.get(messages.size()-1).getMessageId().getEntryId());
            stats.incrementMsgOutCounter(messages.size());
        }
    }

    public CompletableFuture<Void> push(List<Message> messages){

        if(messages != null && !messages.isEmpty()){
            sh.send(Commands.newMessage(consumerKey.getConsumerId(), JSONUtil.toJson(messages)),0);
            updateStats(messages);
        }

        return CompletableFuture.completedFuture(null);
    }


    public CompletableFuture<Void> ackAsync(CommandAck ack){

        PositionKey positionKey = PositionKey.builder()
                .tenantId(ack.getTenantId())
                .subscription(ack.getSubscription())
                .topic(ack.getTopic())
                .build();

        if(ack.getEntryIds() == null || ack.getEntryIds().isEmpty()){
            log.warn("ack entryId is empty ack={}",ack);
            return CompletableFuture.completedFuture(null);
        }

        subscription.ack(positionKey,ack.getEntryIds());

        if(enableMonitor){
            stats.lastAckedTimestamp = System.currentTimeMillis();
            stats.setLastAckedPosition(ack.getEntryIds().get(ack.getEntryIds().size()-1));
        }

        return CompletableFuture.completedFuture(null);
    }

    CompletableFuture<Void> unsubscribe() {

        CompletableFuture<Void> future = new CompletableFuture<>();

        if(subscription.getConsumers().size() == 1  && subscription.getConsumers().get(0) == this){
            close();
            return subscription.topic().unsubscribe(subscription.metaData().getSubscriptionName());
        }else {
            future.completeExceptionally(new MqServerException("当前有多个消费者共享此订阅"));
        }
        return future;

    }

    CompletableFuture<Void> seek(CommandSeek seek){
        subscription.seek(seek.getEntryId());
        return CompletableFuture.completedFuture(null);
    }

    public ConsumerStatsImpl getStats(){
        stats.calculateRate();
        return stats;
    }

    public String getSubscriptionType(){
        return subscriptionType;
    }

    public ConsumerKey getConsumerKey(){
        return consumerKey;
    }

    public void disconnect() {
        log.info("Disconnecting consumer: {}", this);
        //sh.closeConsumer(this);
        try {
            close();
        } catch (Exception e) {
            log.warn("Consumer {} was already closed: {}", this, e.getMessage(), e);
        }
    }

    public void close(){
        log.info("consumer[{}] closing...",this);
        subscription.removeConsumer(this);
        sh.removeConsumer(this);
        log.info("consumer[{}] closed",this);
    }

    public SubscriptionConsumer getSubscriptionConsumer(){
        return subscriptionConsumer;
    }

    @Override
    public String toString() {
        return "Consumer{" +
                ", consumerKey=" + consumerKey +
                ", subscriptionConsumer='" + subscriptionConsumer + '\'' +
                ", subscription='" + subscription + '\'' +
                ", subscriptionType='" + subscriptionType + '\'' +
                '}';
    }
}
