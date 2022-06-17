package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.Consumer;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageId;
import com.laoxin.mq.client.api.MessageListener;
import com.laoxin.mq.client.command.BaseCommand;
import com.laoxin.mq.client.command.CommandMessage;
import com.laoxin.mq.client.command.Commands;
import com.laoxin.mq.client.conf.ConsumerConfigurationData;
import com.laoxin.mq.client.enums.ResultErrorEnum;
import com.laoxin.mq.client.exception.MqClientException;
import com.laoxin.mq.client.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class ConsumerImpl<T> extends AbstractClientConnection implements Consumer<T> {

    private final long consumerId;
    private final MqClientImpl client;
    private final ConsumerConfigurationData<T> conf;
    private final Type type;
    private boolean createTopicIfNotExist = false;
    protected final MessageListener listener;
    final BlockingQueue<Message<T>> incomingMessages;
    protected final ExecutorService listenerExecutor;
    protected final CompletableFuture<Consumer> subscribeFuture;
    private final long subscribeTimeout;
    private boolean orderConsumer;
    private volatile AtomicBoolean listeningMessage = new AtomicBoolean(false);

    public ConsumerImpl(MqClientImpl client, ConsumerConfigurationData conf,Class<T> pojo,ExecutorService listenerExecutor,CompletableFuture<Consumer> subscribeFuture,boolean createTopicIfNotExist){
        super(client,conf.getTopic());
        this.client = client;
        this.consumerId=client.newConsumerId();
        this.conf = conf;
        this.type = ParameterizedTypeImpl.make(List.class,new ParameterizedTypeImpl[]{ParameterizedTypeImpl.make(MessageImpl.class, new Class[]{pojo}, null)},null);
        this.listenerExecutor = listenerExecutor;
        this.createTopicIfNotExist = createTopicIfNotExist;
        this.listener = conf.getMessageListener();
        this.incomingMessages = new ArrayBlockingQueue(1024);
        this.subscribeFuture = subscribeFuture;
        this.orderConsumer = conf.isOrderConsumer();
        this.subscribeTimeout = System.currentTimeMillis() + 10 * 1000;
        openConnection();
    }


    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getSubscription() {
        return conf.getSubscriptionName();
    }

    @Override
    public void unsubscribe() throws MqClientException {
        try {
            unsubscribeAsync().get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof MqClientException) {
                throw (MqClientException) t;
            } else {
                throw new MqClientException(t);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MqClientException(e);
        }
    }

    public CompletableFuture<Void> unsubscribeAsync()  {
        final CompletableFuture<Void> unsubscribeFuture = new CompletableFuture<>();
        if (getState() == State.Closing || getState()  == State.Closed) {
            unsubscribeFuture.completeExceptionally(new MqClientException.AlreadyClosedException("Consumer was already closed"));
            return unsubscribeFuture;
        }
        if(!isConnected()){
            unsubscribeFuture.completeExceptionally(new MqClientException("consumer Not connected to broker"));
            return unsubscribeFuture;
        }
        setState(State.Closing);

        //取消订阅
        long requestId = client.newRequestId();
        final BaseCommand unSubscribe = Commands.newUnSubscribe(topic, getSubscription(), consumerId);

        ch().sendAsync(unSubscribe,requestId).handle((v, e)->{
            if(e != null){
                unsubscribeFuture.completeExceptionally(e.getCause());
                setState(State.Ready);
            }else {
                log.info("取消订阅成功, topic={},subscription={}", topic, getSubscription());
                ch().removeConsumer(consumerId);
                unsubscribeFuture.complete(null);
                setState(State.Closed);
            }
            return null;
        });
        return unsubscribeFuture;
    }


    @Override
    public List<Message<T>> pull() throws MqClientException {
        return pull(null,50);
    }

    private void verifyConsumerState() throws MqClientException {
        switch(this.getState()) {
            case Ready:
            case Connecting:
            default:
                return;
            case Closing:
            case Closed:
                throw new MqClientException.AlreadyClosedException("Consumer already closed");
            case Failed:
            case Uninitialized:
                throw new MqClientException.NotConnectedException("consumer not Connected");
        }
    }

    @Override
    public List<Message<T>> pull(MessageId latestId, int batchSize) throws MqClientException {

        if (listener != null) {
            throw new MqClientException.InvalidConfigurationException(
                    "Cannot use receive() when a listener has been set");
        }
        //校验消费者状态
        verifyConsumerState();

        return pullRemoteMessage(latestId,batchSize);
    }

    //从broker拉取
    private List<Message<T>> pullRemoteMessage(MessageId latestId, int batchSize) throws MqClientException {
        //发送pull命令
        long requestId = client.newRequestId();
        final BaseCommand pull = Commands.newPull(topic, getSubscription(), consumerId, latestId==null?0:((MessageIdImpl)latestId).getEntryId(),batchSize);

        try {
            CommandMessage resp = ch().sendAsync(pull, requestId,CommandMessage.class).get(30,TimeUnit.SECONDS);

            List<Message<T>> message = null;
            if(resp != null ){
                message = decodeMessage(resp);
            }
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MqClientException(e);
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof MqClientException) {
                throw (MqClientException) t;
            } else {
                throw new MqClientException(t);
            }
        } catch (TimeoutException e) {
            throw new MqClientException(e);
        }

    }

    private List<Message<T>> decodeMessage(CommandMessage cmd){
        List<Message<T>> message = JSONUtil.fromJson(cmd.getPayloadAndHeaders(), type);
        return message;
    }

    void messageReceived(CommandMessage cmd, Map<String,Object> properties){
        List<Message<T>> list = decodeMessage(cmd);
        if(list == null || list.size() == 0){
            return;
        }
        try {
            final Message<T> peek = incomingMessages.peek();
            if(orderConsumer && peek != null
                    && list.get(0).getMessageId().compareTo(peek.getMessageId())<=0 ){
                //Duplicate Message
                return;
            }
            for(Message message: list){
                incomingMessages.add(message);
            }
        }finally {
            //触发消息推送
            triggerListener();
        }
    }


    void triggerListener(){
        if(listener != null && !listeningMessage.get()){

            listenerExecutor.execute(()->{
                if(!listeningMessage.compareAndSet(false,true)){
                    return;
                }
                try {
                    triggerListenerSync();
                }finally {
                    listeningMessage.set(false);
                }
            });
        }
    }

    private void triggerListenerSync(){

        while (true){

            Message msg;
            try {
                msg = internalReceive(0,TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("消息出队异常,topic={},Subscription={}", topic, getSubscription(), e);
                return;
            }
            if(msg == null){
                return;
            }

            try {
                if(conf.getFilter() != null){
                    if(!conf.getFilter().accept(msg)){
                        if (log.isDebugEnabled()) {
                            log.debug("[{}][{}] auto ack message for message {} which is filtered", topic, getSubscription(), msg);
                        }
                        ack(msg);
                        continue;
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("[{}][{}] Calling message listener for message {}", topic, getSubscription(), msg);
                }
                listener.onMessage(this, msg);
                //if consumer no ack
                deQueueMsg(msg);
            } catch (Throwable t) {
                log.error("[{}][{}] Message listener error in processing message: {}", topic,  getSubscription(), msg,
                        t);
            }
        }
    }

    private Message internalReceive(long timeout, TimeUnit unit) throws MqClientException {

        Message message;
        try {
            if(orderConsumer){
                message = incomingMessages.peek();
            }else {
                message = incomingMessages.poll(timeout,unit);
            }
            return message;

        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new MqClientException(e);
        }
    }

    private void deQueueMsg(Message msg){
        if(orderConsumer){
            Message message = incomingMessages.peek();
            if(message == msg){
                synchronized (incomingMessages){
                    incomingMessages.poll();
                }
            }
        }
    }

    @Override
    public void ack(Message msg) throws MqClientException {
        deQueueMsg(msg);
        ack(msg.getMessageId());
    }

    public void ack(MessageId msgId) throws MqClientException {
        ack(Arrays.asList(msgId));
    }

    @Override
    public void ack(List<MessageId> msgIds) throws MqClientException {
        try {
            if(msgIds.stream().map(MessageId::getTenantId).collect(Collectors.toSet()).size()>1
                    || msgIds.stream().map(MessageId::getTopic).collect(Collectors.toSet()).size() >1
                ){
                throw new IllegalArgumentException("批量提交的消息id必须都是在同一个topic，同一个租户内");
            }
            ackAsync(msgIds).get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof MqClientException) {
                throw (MqClientException) t;
            } else {
                throw new MqClientException(t);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MqClientException(e);
        }
    }

    public CompletableFuture<Void> ackAsync(List<MessageId> msgIds){

        final CompletableFuture<Void> ackFuture = new CompletableFuture<>();

        if(!isConnected()){
            ackFuture.completeExceptionally(new MqClientException("Consumer not ready. State: "+getState()));
            return ackFuture;
        }

        final MessageIdImpl messageId = (MessageIdImpl)msgIds.get(0);

        List<Long> entryIds = msgIds.stream().map(MessageId::getEntryId).collect(Collectors.toList());

        //发送ack命令
        long requestId = client.newRequestId();
        final BaseCommand ack = Commands.newAck(messageId.getTopic(), getSubscription(), consumerId, messageId.getTenantId(), entryIds);

        ch().sendAsync(ack,requestId).thenRun(()->{
            //ack success
            ackFuture.complete(null);
        }).exceptionally(e->{
            //ack failed
            ackFuture.completeExceptionally(e);
            return null;
        });

        return ackFuture;
    }

    @Override
    public long consumerId() {
        return consumerId;
    }

    @Override
    public void close() throws MqClientException {
        log.info("consumer[{}] closing...",getSubscription());
        try {
            closeAsync().get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof MqClientException) {
                throw (MqClientException) t;
            } else {
                throw new MqClientException(t);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MqClientException(e);
        }
        log.info("consumer[{}] closed",getSubscription());
    }

    public CompletableFuture<Void> closeAsync() {
        if (getState() == State.Closing || getState() == State.Closed) {
            return CompletableFuture.completedFuture(null);
        }
        if(!isConnected()){
            log.info("Closed Consumer which is not connected,topic={},Subscription={}", topic, getSubscription());
            setState(State.Closed);
            client.removeConsumer(this);
            return CompletableFuture.completedFuture(null);
        }
        setState(State.Closing);

        //发送关闭消费者连接
        long requestId = client.newRequestId();
        final BaseCommand closeConsumer = Commands.newCloseConsumer(topic, getSubscription(), consumerId);

        CompletableFuture<Void> closeFuture = new CompletableFuture<>();
        ch().sendAsync(closeConsumer, requestId).handle((v, exception) -> {
            ch().removeConsumer(consumerId);
            if (exception == null ) {
                log.info("Closed Consumer ,topic={},Subscription={}", topic, getSubscription());
                setState(State.Closed);
                closeFuture.complete(null);
                client.removeConsumer(this);
            } else {
                closeFuture.completeExceptionally(exception);
            }
            return null;
        });

        return closeFuture;
    }


    @Override
    protected String getHandlerName() {
        return getSubscription();
    }



    @Override
    public void connectionFailed(MqClientException e) {
        if ((System.currentTimeMillis() > subscribeTimeout || ResultErrorEnum.AUTH_FAILED.getCode().equals(e.getCode())) && subscribeFuture.completeExceptionally(e)) {
            setState(State.Failed);
            client.removeConsumer(this);
        }
    }

    @Override
    public void connectionOpened(MqClientHandler ch) {
        super.connectionOpened(ch);

        ch.registerConsumer(consumerId,this);

        log.info("消费者连接通道已就绪，准备创建订阅:topic={},subscription={}",topic,getSubscription());

        //发送订阅消息
        long requestId = client.newRequestId();
        final BaseCommand subscribe = Commands.newSubscribe(conf.getSubscriptionProperties(),conf.getTenantId(),topic,conf.getTopicType(), getSubscription(), consumerId, conf.getSubscriptionType(), conf.getConsumerName(),conf.getDependencyOnSubscription(),conf.getAckTimeoutMillis()
                ,listener != null);
        ch.sendAsync(subscribe,requestId)
                .thenRun(()->{
                    log.info("subscribe={}创建成功, topic ={},consumerId={}", getSubscription(),topic,consumerId);
                    setState(State.Ready);
                    subscribeFuture.complete(this);
                }).exceptionally(e->{

                    ch.removeConsumer(consumerId);
                    if(reconnectionIfNeed() && !ResultErrorEnum.CONSUMER_EXCLUDE.getCode().equals(MqClientException.translateException(e).getCode())){
                        log.warn("subscribe={}订阅失败,重建连接..., topic ={},remoteUrl={}", getSubscription(),topic,
                                client.getClientConfiguration().getServiceUrl());
                        reconnect(e);
                    }else {
                        if(subscribeFuture.completeExceptionally(e)){
                            log.error("subscribe={}创建失败,state={}, topic ={},remoteUrl={},errMsg={}", getSubscription(),getState(),topic,
                                    client.getClientConfiguration().getServiceUrl(),e.getMessage());
                            setState(State.Failed);
                            ch.closeIfNoneProducerAndConsumer();
                            client.removeConsumer(this);
                        }else {
                            log.warn("subscribe={}订阅失败,重建连接..., topic ={},remoteUrl={}", getSubscription(),topic,
                                    client.getClientConfiguration().getServiceUrl());
                            reconnect(e);
                        }
                    }
                    return null;
        });
    }
}
