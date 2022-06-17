package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.broker.position.PositionKey;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.command.CommandAck;
import com.laoxin.mq.client.command.CommandPull;
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

    public Consumer(MqServerHandler sh,ConsumerKey consumerKey,TopicImpl topic, SubscriptionImpl subscription){
        this.consumerKey = consumerKey;
        this.subscriptionConsumer = subscription.metaData().getConsumer();
        //this.consumerName = subscriptionConsumer.getConsumerName();
        this.subscription = subscription;
        this.subscriptionType = subscription.metaData().getSubscriptionType();
        this.sh = sh;
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

    public CompletableFuture<Void> push(List<Message> messages){

        if(messages != null && !messages.isEmpty()){
            sh.send(Commands.newMessage(consumerKey.getConsumerId(), JSONUtil.toJson(messages)),0);
        }

        return CompletableFuture.completedFuture(null);
    }


    public CompletableFuture<Void> ackAsync(CommandAck ack){

        PositionKey positionKey = PositionKey.builder()
                .tenantId(ack.getTenantId())
                .subscription(ack.getSubscription())
                .topic(ack.getTopic())
                .build();

        subscription.ack(Position.builder()
                .positionKey(positionKey)
                .entryId(ack.getEntryId())
                .build());

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
