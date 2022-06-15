package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.*;
import com.laoxin.mq.client.command.BaseCommand;
import com.laoxin.mq.client.command.CommandSendReceipt;
import com.laoxin.mq.client.command.Commands;
import com.laoxin.mq.client.conf.ProducerConfigurationData;
import com.laoxin.mq.client.enums.ResultErrorEnum;
import com.laoxin.mq.client.exception.MqClientException;
import com.laoxin.mq.client.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ProducerImpl<T> extends DefaultClientConnection implements Producer<T> {

    private final MqClientImpl client;
    private final ProducerConfigurationData conf;
    private final Class<T> pojo;
    private final String producerName;
    private boolean createTopicIfNotExist = false;
    private final long producerId;
    private final CompletableFuture<Producer> producerCreatedFuture;
    private final long createProducerTimeout;

    private long sendTimeOut = 30 * 1000;


    public ProducerImpl(MqClientImpl client, ProducerConfigurationData conf, Class<T> pojo,CompletableFuture<Producer> producerCreatedFuture ,boolean createTopicIfNotExist){
        super(client,conf.getTopic());
        this.client = client;
        this.producerId = client.newProducerId();
        this.conf = conf;
        this.pojo = pojo;
        this.producerName = conf.getProduceName();
        this.producerCreatedFuture = producerCreatedFuture;
        this.createTopicIfNotExist = createTopicIfNotExist;
        this.sendTimeOut = conf.getSendTimeoutMs();
        this.createProducerTimeout = System.currentTimeMillis() + 10 * 1000;
        openConnection();
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getProducerName() {
        return producerName;
    }

    @Override
    public MessageId send(T msg) throws MqClientException {
        return send(Arrays.asList(msg));
    }

    @Override
    public CompletableFuture<MessageId> sendAsync(T msg) {
        return sendAsync(Arrays.asList(msg));
    }

    @Override
    public MessageId send(List<T> batchMsg) throws MqClientException {
        List<Message> list = new ArrayList<>();
        for(T t: batchMsg){
            list.add(MessageBuilder.create().setContent(t).build());
        }
        return sendMessage(list);
    }

    @Override
    public CompletableFuture<MessageId> sendAsync(List<T> batchMsg) {
        List<Message> list = new ArrayList<>();
        for(T t: batchMsg){
            list.add(MessageBuilder.create().setContent(t).build());
        }
        return sendMessageAsync(list);
    }

    @Override
    public MessageSenderBuilder<T> newMessage() {
        return new MessageSenderBuilderImpl(this);
    }

    @Override
    public MessageId send(Message message) throws MqClientException {
        try {
            return sendAsync(message).get();
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

    @Override
    public CompletableFuture<MessageId> sendAsync(Message message) {
        return sendMessageAsync(Arrays.asList(message));
    }

    public MessageId sendMessage(List<Message> list) throws MqClientException{
        try {
            return sendMessageAsync(list).get();
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

    public CompletableFuture<MessageId> sendMessageAsync(List<Message> list) {
        return doSendAsync(JSONUtil.toJson(list));
    }

    public CompletableFuture<MessageId> doSendAsync(String message) {

        CompletableFuture<MessageId> sendFutrue = new CompletableFuture<>();

        //校验连接状态
        if(!isConnected()){
            sendFutrue.completeExceptionally(new MqClientException("producer Not connected to broker"));
            openConnection();
            return sendFutrue;
        }

        //消息发送
        final long requestId = client.newRequestId();
        long createdAt = System.currentTimeMillis();
        BaseCommand send = Commands.newSend(topic,producerId, conf.getTenantId(), createdAt, message);
        ch().sendAsync(send,requestId,sendFutrue,(v,e)->{
            if(sendFutrue.isDone()){
                log.info("send receive timeout, seqId={},producer={}",requestId,producerName);
                return;
            }
            if(log.isDebugEnabled()){
                log.debug("send receive call back,v={}, seqId={},producer={}",v,requestId,producerName);
            }
            if (e != null) {
                sendFutrue.completeExceptionally(e);
            } else {
                sendFutrue.complete((MessageId) v);
            }
        });
        return sendFutrue;
    }

    void ackReceived(CommandSendReceipt receipt,long requestId) {

        MessageId messageId = MessageId.from(topic, receipt.getTenantId(), receipt.getEntryId());

        if(!client.getSendOpsAccept().accept(requestId,messageId,null)){
            log.warn("sendOps closed requestId={},topic={},producer={}",requestId,topic,producerName);
        }

    }

    @Override
    public void close() throws MqClientException {
        log.info("producer[{}] closing...",producerName);
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
        log.info("producer[{}] closed",producerName);
    }

    public CompletableFuture<Void> closeAsync() {
        if (getState() == State.Closing || getState() == State.Closed) {
            return CompletableFuture.completedFuture(null);
        }
        if(!isConnected()){
            log.info("Closed Producer which is not connected,topic={},producer={}", topic, producerName);
            setState(State.Closed);
            client.removeProducer(this);
            return CompletableFuture.completedFuture(null);
        }
        setState(State.Closing);

        //发送关闭生产者连接
        long requestId = client.newRequestId();
        final BaseCommand close = Commands.newCloseProducer(topic, producerId,conf.getTenantId());

        CompletableFuture<Void> closeFuture = new CompletableFuture<>();
        ch().sendAsync(close, requestId).handle((v, exception) -> {
            ch().removeConsumer(producerId);
            if (exception == null ) {
                log.info("Closed Producer ,topic={},producer={}", topic, producerName);
                setState(State.Closed);
                closeFuture.complete(null);
                client.removeProducer(this);
            } else {
                closeFuture.completeExceptionally(exception);
            }
            return null;
        });

        return closeFuture;
    }

    @Override
    public void connectionFailed(MqClientException e) {
        if ((System.currentTimeMillis() > createProducerTimeout  || ResultErrorEnum.AUTH_FAILED.getCode().equals(e.getCode())) && producerCreatedFuture.completeExceptionally(e)) {
            setState(State.Failed);
            client.removeProducer(this);
        }
    }

    @Override
    public void connectionOpened(MqClientHandler ch) {

        super.connectionOpened(ch);
        //通道注册生产者
        ch.registerProducer(producerId,this);

        log.info("生产者连接通道已就绪，准备创建生产者:topic={},producer={}",topic,producerName);

        //发送注册生产者信息
        long requestId = client.newRequestId();
        final BaseCommand producer = Commands.newProducer(topic,conf.getTopicType(), producerId,producerName,conf.getTenantId());

        ch.sendAsync(producer,requestId)
                .thenRun(()->{
                    log.info("producer={}创建成功, topic ={},producerId={}", producerName,topic,producerId);
                    setState(State.Ready);
                    producerCreatedFuture.complete(this);
                })
                .exceptionally(e->{
                    ch.removeProducer(producerId);
                    if(reconnectionIfNeed()){
                        log.warn("producer={}创建失败,重建连接..., topic ={},remoteUrl={}", producerName,topic,
                                client.getClientConfiguration().getServiceUrl());
                        reconnect(e);
                    }else {
                        log.warn("producer={}创建失败,state={}, topic ={},remoteUrl={}", producerName,getState(),topic,
                                client.getClientConfiguration().getServiceUrl());
                        ch.close();
                    }
                    return null;
        });
    }

    @Override
    String getHandlerName() {
        return producerName;
    }
}
