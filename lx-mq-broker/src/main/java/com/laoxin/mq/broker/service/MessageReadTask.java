package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.broker.position.PositionKey;
import com.laoxin.mq.client.api.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MessageReadTask extends AbstractTask {

    private final MessageQueue messageQueue;

    private final SubscriptionImpl subscription;


    private long readMessageLaterTime = 0;


    public MessageReadTask(MessageQueue messageQueue, SubscriptionImpl subscription){
        super();
        this.subscription = subscription;
        this.messageQueue = messageQueue;
    }

    public void doTask(){

        PositionKey positionKey = PositionKey.builder()
                .tenantId(subscription.topic().metaData().getTenantId())
                .subscription(subscription.metaData().getSubscriptionName())
                .topic(subscription.topic().metaData().getTopicName())
                .build();

        //缓存队列最大的消息id
        long lastEntryId = messageQueue.lastId();

        final Position position = Position.builder()
                .positionKey(positionKey)
                .entryId(lastEntryId)
                .build();

        //已经ack的消费位点
        final Optional<Position> optional = subscription.positionOffsetStore().getPosition(positionKey);
        if(optional.isPresent() && optional.get().getEntryId() > lastEntryId){
            position.setEntryId(optional.get().getEntryId());
            //多个broker 实例部署
            //或者手动修改了positionOffset数据才会出现这种现象
            messageQueue.clearLessThanEntryId(optional.get().getEntryId());
        }

        CompletableFuture<List<Message>> future = subscription.pullMessage(position,  50);

        future.thenAccept(this::readSuccess)
                .exceptionally(this::readFailed);
    }

    @Override
    public boolean taskCanDo() {
        if(!subscription.isOnline() || messageQueue.isFull()){
            return false;
        }
        //读取消息出错或者上次拉取的消息为空，会延迟适当的时间再执行
        if(System.currentTimeMillis() < readMessageLaterTime){
            return false;
        }
        return super.taskCanDo();
    }

    @Override
    protected String getTaskName() {
        return messageQueue.getQueueName()+" read message task";
    }

    @Override
    protected void completedException(Exception e) {
        readFailed(e);
    }

    private void readSuccess(List<Message> messages){
        if(messages == null || messages.size() == 0){
            readMessageLaterTime = System.currentTimeMillis() + 1000;
        }else {
            messageQueue.putMessage(messages);
            subscription.triggerPush();
        }
    }

    private Void readFailed(Throwable e){
        log.error("message queue[{}] read error:{} ",subscription,e.getMessage());
        readMessageLaterTime = System.currentTimeMillis() + 1000;
        return null;
    }

}
