package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.api.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultConsumerInterceptContext implements ConsumerInterceptContext {

    private final MessageQueue messageQueue;
    private final TreeMap<Long,MessageOut> pushedMsg;
    private final TreeMap<Long,MessageOut> pulledMsg;


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
    public TreeMap<Long,MessageOut> getPushedMessages() {
        return pushedMsg;
    }

    @Override
    public TreeMap<Long,MessageOut> getPulledMessages() {
        return pulledMsg;
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
