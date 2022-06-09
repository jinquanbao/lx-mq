package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.api.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class MessagePushTask implements Runnable{

    //消息正在读取
    private final AtomicBoolean pushingMessage;

    private final SubscriptionImpl subscription;

    private final MessageQueue messageQueue;

    private final ConsumerInterceptContext interceptContext;

    public MessagePushTask(MessageQueue messageQueue,SubscriptionImpl subscription,ConsumerInterceptContext interceptContext){
        this.messageQueue = messageQueue;
        this.subscription = subscription;
        this.pushingMessage = new AtomicBoolean(false);
        this.interceptContext = interceptContext;
    }

    @Override
    public void run() {
        if(!triggerPush()){
            return ;
        }
        if (!pushingMessage.compareAndSet(false, true)) {
            if(log.isDebugEnabled()){
                log.debug("message queue [{}] is pushingMessage",subscription);
            }
            return;
        }
        try {
            interceptContext.prePush(null);
            pushMessage();
        }catch (Exception e){
            log.error("message queue [{}] push message error :{}",subscription,e.getMessage());
        }finally {
            pushingMessage.set(false);
        }
    }

    private void pushMessage(){

        //推送负载均衡
        Consumer consumer = subscription.allocateConsumer();

        if(consumer != null){

            long now = System.currentTimeMillis();
            final TreeMap<Long, MessageOut> pushedMessages = interceptContext.getPushedMessages();

            List<Message> messages = messageQueue
                    .getMessages(subscription.brokerConf().getDefaultPushSize())
                    .stream()
                    .filter(x->pushIfNecessary(now,x.getMessageId().getEntryId(),pushedMessages))
                    .collect(Collectors.toList());

            if(messages != null && messages.size()>0){

                consumer.push(messages)
                        .thenAccept(v->{
                            interceptContext.pushSuccess(consumer,messages);
                            pushMessage();
                        })
                        .exceptionally(e->{
                            log.error("message queue [{}] push message error :{}",subscription,e.getMessage());
                            return null;
                        });
            }

        }

    }


    private boolean pushIfNecessary(long now,long entryId,TreeMap<Long, MessageOut> pushedMessages){

        //The message is the first push
        if(pushedMessages.isEmpty()){
            return true;
        }
        final Long lastMaxPushEntryId = pushedMessages.lastKey();
        //The message is the first push
        if(entryId > lastMaxPushEntryId){
            return true;
        }
        final MessageOut messageOut = pushedMessages.get(entryId);
        if(messageOut == null){
            return true;
        }

        //Repeat push
        if(subscription.metaData().getAckTimeOut()>0
            && now>(subscription.metaData().getAckTimeOut()+messageOut.getOutTime())){
            return true;
        }

        return false;

    }

    private void pushSuccess(){

    }

    public boolean triggerPush(){

        if(!subscription.isOnline() || pushingMessage.get() || messageQueue.size() == 0){
            return false;
        }

        return true;
    }
}
