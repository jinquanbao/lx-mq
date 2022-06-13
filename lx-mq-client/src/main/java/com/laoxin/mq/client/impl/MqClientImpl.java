package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.*;
import com.laoxin.mq.client.conf.ClientConfigurationData;
import com.laoxin.mq.client.conf.ConsumerConfigurationData;
import com.laoxin.mq.client.conf.ProducerConfigurationData;
import com.laoxin.mq.client.exception.MqClientException;
import com.laoxin.mq.client.util.ExecutorProvider;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MqClientImpl implements MqClient {

    private ClientConfigurationData conf;
    private final Set<Producer<?>> producers;
    private final Set<Consumer<?>> consumers;
    private final ExecutorProvider externalExecutorProvider;
    private final ExecutorProvider internalExecutorProvider;
    private final NettyClientStarter nettyClientStarter;
    private final Timer timer;
    private final AtomicLong producerIdGenerator = new AtomicLong();
    private final AtomicLong consumerIdGenerator = new AtomicLong();
    private final AtomicLong requestIdGenerator = new AtomicLong();
    private final SendOpsAccept sendOpsAccept;
    private final ScheduledExecutorService scheduledExecutorService;

    public MqClientImpl(ClientConfigurationData conf){
        this.conf = conf;
        this.producers = Collections.newSetFromMap(new ConcurrentHashMap());
        this.consumers = Collections.newSetFromMap(new ConcurrentHashMap());
        externalExecutorProvider = new ExecutorProvider(conf.getListenerThreads(), "mq-external-listener");
        internalExecutorProvider = new ExecutorProvider(conf.getListenerThreads(), "mq-internal-listener");
        this.nettyClientStarter = new NettyClientStarter(this);
        this.timer = new HashedWheelTimer(new DefaultThreadFactory("mq-timer"), 1, TimeUnit.MILLISECONDS);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"mq-scheduled"));
        this.sendOpsAccept=new SendOpsAccept(scheduledExecutorService);
    }

    @Override
    public ProducerBuilder<String> newProducer() {
        return new ProducerBuilderImpl(this,String.class);
    }

    @Override
    public <T> ProducerBuilder<T> newProducer(Class<T> pojo) {
        return new ProducerBuilderImpl(this,pojo);
    }

    @Override
    public ConsumerBuilder<String> newConsumer() {
        return new ConsumerBuilderImpl(this,String.class);
    }

    @Override
    public <T> ConsumerBuilder<T> newConsumer(Class<T> pojo) {
        return new ConsumerBuilderImpl(this,pojo);
    }

    public <T> Consumer<T> subscribe(ConsumerConfigurationData<T> conf, Class<T> pojo) throws MqClientException {

        try {
            return subscribeAsync(conf,pojo).get();
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
        }
    }

    public <T> CompletableFuture<Consumer> subscribeAsync(ConsumerConfigurationData<T> conf, Class<T> pojo) {

        CompletableFuture<Consumer> consumerSubscribedFuture = new CompletableFuture<>();
        Consumer consumer =  new ConsumerImpl(this,conf,pojo,externalExecutorProvider.getExecutor(),consumerSubscribedFuture,true);
        consumers.add(consumer);

        return consumerSubscribedFuture;
    }

    public <T> Producer<T> createProducer(ProducerConfigurationData conf, Class<T> pojo) throws MqClientException {
        try {
            return createProducerAsync(conf,pojo).get();
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
        }
    }

    public <T> CompletableFuture<Producer> createProducerAsync(ProducerConfigurationData conf, Class<T> pojo) {

        CompletableFuture<Producer> producerFuture = new CompletableFuture<>();
        Producer producer = new ProducerImpl(this,conf,pojo,producerFuture,true);
        producers.add(producer);
        return producerFuture;
    }

    @Override
    public void close() {
        log.info("mq client closing...");
        synchronized (producers){
                producers.forEach(p-> {
                    try {
                        p.close();
                    } catch (MqClientException e) {
                        log.error("producer close error:{}",e.getMessage());
                    }
                });
        }
        synchronized (consumers){
            consumers.forEach(c-> {
                try {
                    c.close();
                } catch (MqClientException e) {
                    log.error("consumer close error:{}",e.getMessage());
                }
            });
        }
        externalExecutorProvider.shutdownNow();
        internalExecutorProvider.shutdownNow();
        scheduledExecutorService.shutdownNow();
        log.info("mq client closed");
    }


    public ClientConfigurationData getClientConfiguration(){
        return conf;
    }

    public SendOpsAccept getSendOpsAccept(){
        return sendOpsAccept;
    }

    protected CompletableFuture<MqClientHandler> getConnection() {

        return nettyClientStarter.createConnection();
    }

    public long newProducerId() {
        return producerIdGenerator.getAndIncrement();
    }

    public long newConsumerId() {
        return consumerIdGenerator.getAndIncrement();
    }

    public long newRequestId() {
        return requestIdGenerator.getAndIncrement();
    }


    Timer timer(){
        return timer;
    }

    void removeProducer(Producer producer) {
        synchronized (producers) {
            producers.remove(producer);
        }
    }

    void removeConsumer(Consumer consumer) {
        synchronized (consumer) {
            consumers.remove(consumer);
        }
    }
}
