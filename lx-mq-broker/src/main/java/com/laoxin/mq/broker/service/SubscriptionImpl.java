package com.laoxin.mq.broker.service;


import com.laoxin.mq.broker.config.BrokerConfigurationData;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.broker.position.PositionKey;
import com.laoxin.mq.broker.position.PositionOffsetStore;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.enums.ResultErrorEnum;
import com.laoxin.mq.client.enums.SubscriptionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SubscriptionImpl implements Subscription{

    private final TopicImpl topic;
    private final PositionOffsetStore positionOffsetStore;
    private SubscriptionMetaData subscriptionMetaData;
    private String subscriptionType;
    private final CopyOnWriteArrayList<Consumer> consumers;
    private final CompletableFuture<Void> closeFuture;
    private final BrokerConfigurationData brokerConf;
    private final AllocateConsumerStrategy allocateConsumerStrategy;
    private final MessageQueue messageQueue;
    private final MessagePushTask pushTask;
    private final MessageReadTask readTask;
    private final ExecutorService pushMessageExecutor;
    private final ExecutorService readMessageExecutor;
    private final MetaStore metaStore;
    private final ConsumerInterceptContext interceptContext;


    public SubscriptionImpl(TopicImpl topic, BrokerService service, SubscriptionMetaData subscriptionMetaData){
        this.topic = topic;
        this.brokerConf = service.conf();
        this.positionOffsetStore = service.positionOffsetStore();
        this.allocateConsumerStrategy = service.loadBalanceManager().consumerLoadBalanceStrategy();
        this.subscriptionMetaData = subscriptionMetaData;
        this.subscriptionType = subscriptionMetaData.getSubscriptionType();
        this.consumers = new CopyOnWriteArrayList<>();
        this.closeFuture = new CompletableFuture<>();
        this.messageQueue = new MessageQueue(this);
        this.interceptContext = new DefaultConsumerInterceptContext(messageQueue);
        this.readTask = new MessageReadTask(this.messageQueue,this);
        this.pushTask = new MessagePushTask(this.messageQueue,this,interceptContext);
        this.pushMessageExecutor = service.pushMessageExecutor();
        this.readMessageExecutor = service.readMessageExecutor();
        this.metaStore = service.metaStore();

    }



    @Override
    public void registerConsumer(Consumer consumer) {

        switch (SubscriptionType.getEnum(consumer.getSubscriptionType())){

            case Direct:
                clearIfSubscriptionTypeChanged(consumer.getSubscriptionType());
                if(!consumers.isEmpty()){
                    throw new MqServerException(ResultErrorEnum.CONSUMER_EXCLUDE);
                }
                consumers.add(consumer);
                break;

            case Shared:
                clearIfSubscriptionTypeChanged(consumer.getSubscriptionType());
                consumers.add(consumer);
                break;

            default:
                throw new MqServerException("Unsupported subscription type");
        }
    }

    private void clearIfSubscriptionTypeChanged(String subscriptionType){
        if(!Objects.equals(this.subscriptionType,subscriptionType) ){
            this.subscriptionType = subscriptionType;
            //TODO consumer close?
            consumers.clear();
        }
        clearCacheMessage();
    }

    private void clearCacheMessage(){
        if(consumers.isEmpty()){
            messageQueue.clear();
            interceptContext.clearForce();
        }
    }

    @Override
    public boolean removeConsumer(Consumer consumer) {
        consumers.remove(consumer);
        clearCacheMessage();
        CompletableFuture<Consumer> future = new CompletableFuture<>();

        SubscriptionKey subscriptionKey = SubscriptionKey.builder()
                .subscriptionName(subscriptionMetaData.getSubscriptionName())
                .tenantId(topic.metaData().getTenantId())
                .topicName(topic.metaData().getTopicName()).build();
        metaStore.removeSubscriptionConsumer(subscriptionKey,consumer.getSubscriptionConsumer(),(v,e)->{
            log.info("remove Subscription Consumer success");
            future.complete(consumer);
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MqServerException(e);
        } catch (ExecutionException e) {
            throw new MqServerException(e);
        }
        return true;
    }

    @Override
    public List<Consumer> getConsumers() {
        return consumers;
    }

    @Override
    public CompletableFuture<List<Message>> pullMessage(Position position, int size) {

        if(position.getEntryId() == 0){

            Optional<Position> optional = positionOffsetStore().getPosition(position.getPositionKey());

            optional.ifPresent(x->{
                position.setEntryId(x.getEntryId());
            });
        }

        Long maxEntryId = null;

        if(StringUtils.hasText(metaData().getDependencyOnSubscription())){
            Optional<Position> dependencyPosition = positionOffsetStore().getPosition(PositionKey.builder()
                    .tenantId(position.getPositionKey().getTenantId())
                    .topic(position.getPositionKey().getTopic())
                    .subscription(metaData().getDependencyOnSubscription())
                    .build());

            if(!dependencyPosition.isPresent()){
                log.debug("当前订阅[{}]依赖的订阅[{}]还未消费，等待依赖订阅消费,",position,metaData().getDependencyOnSubscription());
                return CompletableFuture.completedFuture(null);
            }

            if(position.compareTo(dependencyPosition.get()) >=0 ){
                log.debug("当前订阅消费位点[{}]已经赶上依赖订阅消费位点[{}]，等待依赖订阅消费,",position,dependencyPosition.get());
                return CompletableFuture.completedFuture(null);
            }

            maxEntryId = dependencyPosition.get().getEntryId();
        }

        return topic.pullMessage(position,subscriptionMetaData.getSubscriptionProperties(),maxEntryId,size);
    }

    public CompletableFuture<List<Message>> pullCacheMessage(Position position, int size){
        if(position.getEntryId() == 0){
            Optional<Position> optional = positionOffsetStore().getPosition(position.getPositionKey());
            optional.ifPresent(x->{
                position.setEntryId(x.getEntryId());
            });
        }
        if(position.getEntryId()< messageQueue.getLastRemoveId()){
            //位置已经被消费过，说明是要查历史消费信息
            return pullMessage(position,size);
        }else if(messageQueue.isFull() && position.getEntryId()>messageQueue.lastId()) {
            //缓存队列已满，消费位移大于缓存最大消费位点，判断缓存队列的数据是否都已被消费
            //如果都被消费了，清除缓存队列消息
            Optional<Position> optional = positionOffsetStore().getPosition(position.getPositionKey());
            optional.ifPresent(x->{
                if(x.getEntryId() >= messageQueue.lastId()){
                    log.info("[{}] cache message already consumed, try clear...",messageQueue.getQueueName());
                    messageQueue.clear();
                }
            });
            return CompletableFuture.completedFuture(null);

        }else{
            return CompletableFuture.completedFuture(messageQueue.getMessagesFrom(position.getEntryId(),null,size));
        }
    }

    @Override
    public void triggerPush() {
        if(subscriptionMetaData.isEnablePush()
            && pushTask.taskCanDo()){
            pushMessageExecutor.execute(pushTask);
        }
    }

    public void triggerReadMessage() {
        if(readTask.taskCanDo()){
            readMessageExecutor.execute(readTask);
        }
    }

    @Override
    public void ack(Position position) {
        messageQueue.remove(position.getEntryId());
        interceptContext.ack(position.getEntryId());
        positionOffsetStore.persist(position);
        this.triggerPush();
    }

    @Override
    public void ack(PositionKey positionKey, List<Long> entryIds) {
        messageQueue.remove(entryIds);
        interceptContext.ack(entryIds);
        positionOffsetStore.persist(Position.builder()
                .positionKey(positionKey)
                .entryId(entryIds.stream().max((o1,o2)->o1.compareTo(o2)).get())
                .build());
        this.triggerPush();
    }

    @Override
    public boolean seek(long entryId) {
        //重置消费位移前将消息读取和消息推送任务暂停
        if(!readTask.pause()){
            throw new MqServerException("readTask pause failed");
            //return false;
        }
        try {
            if(!pushTask.pause()){
                throw new MqServerException("pushTask pause failed");
                //return false;
            }
            try {
                for(int i=0;i<100;i++){

                    //等待任务执行完
                    if(readTask.isRunning() || pushTask.isRunning() ){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }

                    PositionKey positionKey = PositionKey.builder()
                            .tenantId(topic.metaData().getTenantId())
                            .topic(topic.metaData().getTopicName())
                            .subscription(metaData().getSubscriptionName())
                            .build();

                    final boolean seek = positionOffsetStore.seek(Position.builder()
                            .positionKey(positionKey)
                            .entryId(entryId)
                            .build());
                    //缓存重置
                    if(!seek){
                        throw new MqServerException("persist seek position failed");
                    }
                    messageQueue.clear();
                    messageQueue.setLastRemoveId(entryId);
                    interceptContext.clearForce();
                    return seek;
                }
            }finally {
                pushTask.cancelPause();
            }
        }finally {
            readTask.cancelPause();
        }
        throw new MqServerException("readTask or pushTask wait pause time out");
        //return false;
    }

    @Override
    public CompletableFuture<Void> close() {
        if (log.isDebugEnabled()) {
            log.debug("Closing subscription {}", this);
        }
        closeFuture.complete(null);
        return closeFuture;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            consumers.forEach(consumer -> consumer.disconnect());
            future.complete(null);
        }catch (Exception e){
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public boolean isOnline() {
        return !consumers.isEmpty();
    }

    @Override
    public Consumer allocateConsumer() {
        return allocateConsumerStrategy.allocate(consumers);
    }

    @Override
    public void refreshMetaData(SubscriptionMetaData subscriptionMetaData) {
        this.subscriptionMetaData = subscriptionMetaData;
    }

    SubscriptionMetaData metaData(){
        return subscriptionMetaData;
    }

    TopicImpl topic(){
        return topic;
    }

    PositionOffsetStore positionOffsetStore(){
        return positionOffsetStore;
    }

    BrokerConfigurationData brokerConf(){
        return brokerConf;
    }

    ConsumerInterceptContext consumerInterceptContext(){
        return interceptContext;
    }

    @Override
    public String toString() {
        return "SubscriptionImpl{" +
                "topic=" + topic.metaData().getTopicName() +
                ", subscription=" + subscriptionMetaData.getSubscriptionName() +
                ", tenantId=" +topic.metaData().getTenantId() +
                '}';
    }
}
