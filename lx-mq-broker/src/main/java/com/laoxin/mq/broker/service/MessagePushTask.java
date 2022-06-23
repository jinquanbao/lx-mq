package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.api.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
public class MessagePushTask extends AbstractTask{

    //消息正在读取
    private final SubscriptionImpl subscription;

    private final MessageQueue messageQueue;

    private final ConsumerInterceptContext interceptContext;

    public MessagePushTask(MessageQueue messageQueue,SubscriptionImpl subscription,ConsumerInterceptContext interceptContext){
        super();
        this.messageQueue = messageQueue;
        this.subscription = subscription;
        this.interceptContext = interceptContext;
    }

    @Override
    public void doTask() {

        try {
            interceptContext.prePush(null);
            pushMessage();
        }catch (Exception e){
            log.error("message queue [{}] push message error :{}",subscription,e.getMessage());
        }
    }

    @Override
    protected String getTaskName() {
        return messageQueue.getQueueName()+" push message task";
    }

    @Override
    protected void completedException(Exception e) {
        log.error("message queue [{}] push message error :{}",subscription,e.getMessage());
    }

    private void pushMessage(){

        if(pause.get()){
            log.info("[{}] has been paused",getTaskName());
            return;
        }

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

            if(messages == null || messages.isEmpty()){
                return;
            }

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


    private boolean pushIfNecessary(long now,long entryId,TreeMap<Long, MessageOut> pushedMessages){

        //The message is the first push
        if(pushedMessages.isEmpty()){
            return true;
        }
        try {
            //这里可能会报NoSuchElementException
            final Long lastMaxPushEntryId = pushedMessages.lastKey();
            //The message is the first push
            if(entryId > lastMaxPushEntryId){
                return true;
            }
        }catch (Exception e){

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

    @Override
    public boolean taskCanDo() {
        if(!subscription.isOnline() || messageQueue.size() == 0){
            return false;
        }
        return super.taskCanDo();
    }

}
