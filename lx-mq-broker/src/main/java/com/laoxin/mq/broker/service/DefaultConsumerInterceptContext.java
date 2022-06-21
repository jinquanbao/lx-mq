package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.api.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DefaultConsumerInterceptContext implements ConsumerInterceptContext {

    private final MessageQueue messageQueue;
    private final TreeMap<Long,MessageOut> pushedMsg;
    private final TreeMap<Long,MessageOut> pulledMsg;

    private final long expireTime = 7 * 24 * 3600 * 1000;

    public DefaultConsumerInterceptContext(MessageQueue messageQueue){
        this.messageQueue = messageQueue;
        this.pulledMsg = new TreeMap<>();
        this.pushedMsg = new TreeMap<>();
    }

    @Override
    public void prePull(Consumer consumer) {

    }

    @Override
    public void pullSuccess(Consumer consumer, List<Message> messages) {
        synchronized (pulledMsg){
            messageOut(messages, pulledMsg);
        }
    }

    private void messageOut(List<Message> messages, TreeMap<Long, MessageOut> msgOutMap) {
        long now = System.currentTimeMillis();
        messages.forEach(message -> {
            final MessageOut messageOut = msgOutMap.computeIfAbsent(message.getMessageId().getEntryId(), x -> MessageOut.builder()
                    .firstOutTime(now)
                    .outTime(now)
                    .outCounter(new AtomicInteger())
                    .build());
            messageOut.setOutTime(now);
            messageOut.getOutCounter().incrementAndGet();
        });
    }

    @Override
    public void prePush(Consumer consumer) {
        long minId = messageQueue.firstId();
        if(minId == 0){
            pushedMsg.clear();
        }else {
            clearLessThanEntryId(pushedMsg,minId);
        }
    }

    @Override
    public void pushSuccess(Consumer consumer, List<Message> messages) {
        messageOut(messages, pushedMsg);
    }

    @Override
    public void ack(long entryId) {

        pushedMsg.remove(entryId);

        synchronized (pulledMsg){
            pulledMsg.remove(entryId);
        }

    }

    @Override
    public void ack(List<Long> entryIds) {
        synchronized (pulledMsg){
            for(Long entryId: entryIds){
                pulledMsg.remove(entryId);
                pushedMsg.remove(entryId);
            }
        }
    }

    @Override
    public TreeMap<Long,MessageOut> getPushedMessages() {
        return pushedMsg;
    }

    @Override
    public TreeMap<Long,MessageOut> getPulledMessages() {
        return pulledMsg;
    }

    @Override
    public CompletableFuture<Void> clear() {
        final int pullCount = clearExpireMessage(pulledMsg);
        final int pushCount = clearExpireMessage(pushedMsg);
        log.info("messageQueue[{}] clear pull out message count [{}],clear push out message count [{}]"
                ,messageQueue.getQueueName(),pullCount,pushCount);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> clearForce() {
        pushedMsg.clear();
        pulledMsg.clear();
        log.info("messageQueue[{}] clear out message success",messageQueue.getQueueName());
        return CompletableFuture.completedFuture(null);
    }

    private int clearExpireMessage(Map<Long,MessageOut> map){
        int i = 0;
        long now = System.currentTimeMillis();
        synchronized (map){
            final Iterator<Map.Entry<Long, MessageOut>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()){
                MessageOut out = iterator.next().getValue();
                if(now > out.getFirstOutTime() + expireTime){
                    //expireTime 还未确认消费
                    iterator.remove();
                    i++;
                }
            }
        }
        return i;
    }

    private void clearLessThanEntryId(Map<Long,MessageOut> map,long entryId){

        final Iterator<Map.Entry<Long, MessageOut>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Long id = iterator.next().getKey();
            if(id.compareTo(entryId) >= 0){
                break;
            }
            //已经确认消费，移除
            iterator.remove();
        }

    }
}
