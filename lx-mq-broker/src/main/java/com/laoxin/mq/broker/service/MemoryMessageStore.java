package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.enums.StoreType;
import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageId;
import com.laoxin.mq.client.impl.MessageIdImpl;
import com.laoxin.mq.client.impl.MessageImpl;
import com.laoxin.mq.client.util.JSONUtil;
import org.springframework.core.ParameterizedTypeReference;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryMessageStore implements MessageStore {

    private Map<TopicKey, PriorityQueue<Message>> msgMap = new ConcurrentHashMap<>();

    private Map<TopicKey, AtomicLong> idMap = new ConcurrentHashMap<>();

    @Override
    public boolean storageType(String storageType) {
        return StoreType.memory.name().equalsIgnoreCase(storageType);
    }

    @Override
    public CompletableFuture<List<Message>> readMessage(Position position, Long maxEntryId, int readSize) {

        final PriorityQueue<Message> queue = msgMap.get(TopicKey.builder().tenantId(position.getPositionKey().getTenantId())
                .topicName(position.getPositionKey().getTopic()).build());

        if(queue == null){
            return CompletableFuture.completedFuture(null);
        }

        List<Message> list = new ArrayList<>();
        final Iterator<Message> iterator = queue.iterator();
        while (iterator.hasNext()){
            final Message next = iterator.next();
            if(maxEntryId != null && maxEntryId.compareTo(next.getMessageId().getEntryId()) <0 ){
                break;
            }
            if(((Long)position.getEntryId()).compareTo(next.getMessageId().getEntryId()) <0 ){
                list.add(next);
                if(list.size() >= readSize){
                    break;
                }
            }
        }
        return CompletableFuture.completedFuture(list);
    }

    @Override
    public MessageIdImpl writeMessage(TopicKey topicKey, String message) {
        List<Message<Object>> messages = JSONUtil.fromJson(message, new ParameterizedTypeReference<List<MessageImpl<Object>>>() {
        });
        if(null == messages || messages.isEmpty()){
            return null;
        }
        MessageIdImpl result = null;
        AtomicLong id = idMap.computeIfAbsent(topicKey, x -> new AtomicLong());
        synchronized (id){
            PriorityQueue<Message> queue = msgMap.computeIfAbsent(topicKey, x -> new PriorityQueue<Message>(Comparator.comparing(Message::getMessageId)));
            for(Message msg: messages){
                long l = id.incrementAndGet();
                MessageId messageId = MessageId.from(topicKey.getTopicName(), topicKey.getTenantId(), l);
                ((MessageImpl)msg).setMessageId((MessageIdImpl)messageId);
                queue.add(msg);
                result = (MessageIdImpl)messageId;
            }
        }
        return result;
    }
}
