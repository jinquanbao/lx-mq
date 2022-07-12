package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.broker.position.PositionKey;
import com.laoxin.mq.broker.stats.ConsumerStatsImpl;
import com.laoxin.mq.broker.stats.ConsumerStatsRecorder;
import com.laoxin.mq.broker.trace.ConsumerTraceLogInfo;
import com.laoxin.mq.broker.trace.TraceLogContext;
import com.laoxin.mq.broker.trace.TraceLogConvert;
import com.laoxin.mq.broker.trace.TraceStepEnum;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageId;
import com.laoxin.mq.client.command.CommandAck;
import com.laoxin.mq.client.command.CommandPull;
import com.laoxin.mq.client.command.CommandSeek;
import com.laoxin.mq.client.command.Commands;
import com.laoxin.mq.client.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class Consumer {

    private final MqServerHandler sh;

    private final SubscriptionImpl subscription;

    private ConsumerKey consumerKey;
    //private final String consumerName;
    private String subscriptionType;
    private SubscriptionConsumer subscriptionConsumer;
    private final String topicName;
    private final ConsumerStatsRecorder stats;

    private final boolean enableMonitor;
    private final TraceLogContext traceLogContext;

    public Consumer(MqServerHandler sh,ConsumerKey consumerKey,TopicImpl topic, SubscriptionImpl subscription){
        this.consumerKey = consumerKey;
        this.subscriptionConsumer = subscription.metaData().getConsumer();
        this.topicName = topic.metaData().getTopicName();
        this.subscription = subscription;
        this.subscriptionType = subscription.metaData().getSubscriptionType();
        this.sh = sh;
        this.enableMonitor = topic.brokerConf().isEnableMonitor();
        this.stats = enableMonitor?new ConsumerStatsImpl(consumerKey.getTenantId(),subscriptionConsumer.getConsumerName()):ConsumerStatsRecorder.disabledInstance();
        this.traceLogContext = topic.brokerService().traceLogManager().traceLogContext();
        afterPropertiesSet();
    }

    private void afterPropertiesSet(){
        stats.setAddress(subscriptionConsumer.getAddress());
        stats.setConnectedTimestamp(System.currentTimeMillis());
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
        updateStats(messages,null);
        logMessageOut(messages);
    }

    public void pullSendFailed(Throwable e){
        updateStats(null,e);
    }

    private void updateStats(List<Message> messages,Throwable e){
        if(e != null){
            stats.setException(e);
            stats.setExceptionTimestamp(System.currentTimeMillis());
        }
        else if(messages != null && !messages.isEmpty()){
            stats.setException(null);
            stats.setLastMsgOutTimestamp(System.currentTimeMillis());
            stats.setLastMsgOutPosition(messages.get(messages.size()-1).getMessageId().getEntryId());
            stats.incrementMsgOutCounter(messages.size());
        }
    }

    private void logMessageOut(List<Message> messages){
        if(messages == null || messages.isEmpty()){
            return;
        }
        traceLogContext.log(TraceLogConvert.
                convert(messages,null,
                        consumerTraceLog(TraceStepEnum.msg_outed),
                        TraceStepEnum.msg_outed));
    }

    private ConsumerTraceLogInfo consumerTraceLog(TraceStepEnum stepEnum){
        final long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return ConsumerTraceLogInfo.builder()
                .subscriptionName(subscriptionConsumer.getConsumerName())
                .subscriptionType(subscriptionType)
                .status(stepEnum == TraceStepEnum.msg_acked?1:0)
                .consumerName(subscriptionConsumer.getConsumerName())
                .address(subscriptionConsumer.getAddress())
                .msgOutTimestamp(stepEnum == TraceStepEnum.msg_outed?now:null)
                .ackTimestamp(stepEnum == TraceStepEnum.msg_acked?now:null)
                .build();
    }

    public CompletableFuture<Void> push(List<Message> messages){

        if(messages != null && !messages.isEmpty()){
            sh.send(Commands.newMessage(consumerKey.getConsumerId(), JSONUtil.toJson(messages)),0);
            updateStats(messages,null);
            logMessageOut(messages);
        }

        return CompletableFuture.completedFuture(null);
    }

    public void pushSendFailed(Throwable e){
        updateStats(null,e);
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

        //stats
        stats.setLastAckedTimestamp(System.currentTimeMillis());
        stats.setLastAckedPosition(ack.getEntryIds().get(ack.getEntryIds().size()-1));
        //trace log
        List<MessageId> collect = ack.getEntryIds().stream().map(x -> MessageId.from(ack.getTopic(), ack.getTenantId(), x)).collect(Collectors.toList());
        traceLogContext.log(TraceLogConvert.convert(collect,consumerTraceLog(TraceStepEnum.msg_acked),TraceStepEnum.msg_acked));

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

    public ConsumerStatsRecorder getStats(){
        stats.intervalCalculate();
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
