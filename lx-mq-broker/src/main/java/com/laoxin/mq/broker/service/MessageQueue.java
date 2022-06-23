package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.client.api.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class MessageQueue {

    private final SubscriptionImpl subscription;

    private final TreeMap<Long, Message> cacheMsg;

    private final ReadWriteLock lock;

    private long lastRemoveId;

    private final String queueName;


    public MessageQueue(SubscriptionImpl subscription){
        this.subscription = subscription;
        this.cacheMsg = new TreeMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.queueName = subscription.topic().metaData().getTopicName()+"@" + subscription.metaData().getSubscriptionName()
                +"@" +subscription.topic().metaData().getTenantId();
    }

    public String getQueueName(){
        return queueName;
    }

    public boolean isFull(){
        return size()>=subscription.brokerConf().getConsumerQueueThresholdSzie();
    }

    public int size(){
        return cacheMsg.size();
    }

    public long firstId(){
        try {
            lock.readLock().lockInterruptibly();
            try {
                if(!cacheMsg.isEmpty()){
                    return cacheMsg.firstKey();
                }
            }finally {
                lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("read message queue[{}] interrupted ",subscription);
            throw new MqServerException("read message queue interrupted");
        }
        return 0;
    }

    public long lastId(){
        try {
            lock.readLock().lockInterruptibly();
            try {
                if(!cacheMsg.isEmpty()){
                    return cacheMsg.lastKey();
                }
            }finally {
                lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("read message queue[{}] interrupted ",subscription);
            throw new MqServerException("read message queue interrupted");
        }
        return 0;
    }

    public Message remove(long entryId){

        return remove(Arrays.asList(entryId));
    }

    public Message remove(List<Long> entryIds){

        try {
            lock.writeLock().lockInterruptibly();
            try {
                Message remove = null;
                for(Long entryId: entryIds){
                    remove = cacheMsg.remove(entryId);
                    if(remove != null){
                        lastRemoveId = entryId;
                    }
                }
                return remove;
            }finally {
                lock.writeLock().unlock();
            }
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("remove entry on the message queue[{}] interrupted ",subscription);
        }
        return null;
    }

    public List<Message> getMessages(int size){
        return getMessagesFrom(null,null,size);
    }

    //(beginEntryId,endEntryId]
    public List<Message> getMessagesFrom(Long beginEntryId,Long endEntryId,int size){

        if(cacheMsg.isEmpty()){
            return null;
        }
        List<Message> result = new ArrayList<>(size>cacheMsg.size()?cacheMsg.size():size);
        int i =0;
        try {
            lock.readLock().lockInterruptibly();
            try {
                for(Map.Entry<Long,Message> entry: cacheMsg.entrySet()){
                    //缓存中de值比开始ID还小
                    if(beginEntryId != null && entry.getKey().compareTo(beginEntryId)<=0){
                        continue;
                    }
                    //缓存中的值已大于结束ID的值
                    if(endEntryId != null && entry.getKey().compareTo(endEntryId)>0){
                        break;
                    }
                    if(++i>size){
                        break;
                    }
                    result.add(entry.getValue());
                }
            }finally {
                lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("get message from queue[{}] interrupted ",subscription);
        }
        return result;
    }

    public void clearLessThanEntryId(long entryId){

        try {
            lock.writeLock().lockInterruptibly();
            try {
                final Iterator<Map.Entry<Long, Message>> iterator = cacheMsg.entrySet().iterator();
                while (iterator.hasNext()){
                    final Long key = iterator.next().getKey();
                    if(key != null && key.compareTo(entryId) >0){
                        break;
                    }
                    //小于指定id的移除
                    Message remove = cacheMsg.remove(key);
                    if(remove != null){
                        lastRemoveId = key;
                    }
                }
            }finally {
                lock.writeLock().unlock();
            }
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("clear message queue[{}] interrupted ",subscription);
        }
    }

    public void clear(){
        try {
            lock.writeLock().lockInterruptibly();
            try {
                cacheMsg.clear();
                log.info("messageQueue [{}] clear success ",getQueueName());
            }finally {
                lock.writeLock().unlock();
            }
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("clear message queue[{}] interrupted ",subscription);
        }
    }

    public void putMessage(List<Message> messages){

        if(messages == null || messages.isEmpty()){
            return;
        }
        try {
            lock.writeLock().lockInterruptibly();
            try {
                for(Message msg : messages){
                    cacheMsg.put(msg.getMessageId().getEntryId(),msg);
                }
            }finally {
                lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("read message queue[{}] interrupted ",subscription);
            return;
        }

    }

    public long getLastRemoveId(){
        return lastRemoveId;
    }

    public void setLastRemoveId(long lastRemoveId) {
        this.lastRemoveId = lastRemoveId;
    }
}
