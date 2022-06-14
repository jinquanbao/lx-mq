package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.authentication.AuthenticationService;
import com.laoxin.mq.broker.authentication.AuthenticationServiceImpl;
import com.laoxin.mq.broker.authentication.DefaultAuthenticationProviderManager;
import com.laoxin.mq.broker.config.BrokerConfigurationData;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.position.PositionOffsetStore;
import com.laoxin.mq.broker.spring.SpringContext;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.util.FutureUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class BrokerService implements Closeable {

    private NettyServerStarter serverStarter = null;

    private final Map<TopicKey, CompletableFuture<Topic>> topics;

    private final BrokerConfigurationData conf;

    private final MetaStore metaStore;
    private final PositionOffsetStore positionOffsetStore;
    private final StoreManager storeManager;
    private final LoadBalanceManager loadBalanceManager;
    private final Worker messagePushWorker;
    private final Worker messageReadWorker;
    private final ExecutorService pushMessageExecutor;
    private final ExecutorService readMessageExecutor;
    private final SpringContext springContext;
    private final AuthenticationService authenticationService;



    public BrokerService(DataSource dataSource, SpringContext springContext, BrokerConfigurationData conf){
        this.conf = conf;
        this.authenticationService = new AuthenticationServiceImpl(new DefaultAuthenticationProviderManager(dataSource,conf));
        this.serverStarter = new NettyServerStarter(this);
        this.topics = new ConcurrentHashMap<>();
        this.storeManager = new DefaultStoreManager(dataSource,springContext,conf);
        this.metaStore = storeManager.getMetaStore();
        this.positionOffsetStore = storeManager.getPositionOffsetStore();
        this.loadBalanceManager = new DefaultLoadBalanceManager();
        this.messagePushWorker = new MessagePushWorker(this.topics);
        this.messageReadWorker = new MessageReadWorker(this.topics);
        this.pushMessageExecutor = Executors.newSingleThreadExecutor();
        this.readMessageExecutor = Executors.newSingleThreadExecutor();
        this.springContext = springContext;
    }

    public void start(){
        messageReadWorker.start();
        messagePushWorker.start();
        metaStore.start();
        serverStarter.start();
    }



    @Override
    public void close() throws IOException {
        log.info("mq broker closing...");
        serverStarter.close();
        messagePushWorker.close();
        messageReadWorker.close();
        pushMessageExecutor.shutdownNow();
        readMessageExecutor.shutdownNow();
        closeTopic().thenAccept(v->
            log.info("all topic closed success")
        ).exceptionally(e->{
            log.info("topic closing completeExceptionally:{}",e.getMessage());
            return null;
        });
        log.info("mq broker closed");
    }

    public CompletableFuture<Topic> getTopic(TopicMetaData metaData){

        TopicKey topicKey = TopicKey.builder()
                .tenantId(metaData.getTenantId())
                .topicName(metaData.getTopicName())
                .build();

        CompletableFuture<Topic> future = topics.get(topicKey);
        if(future != null){
            return future;
        }

        if(TopicType.getEnum(metaData.getTopicType()) == null){
            future = new CompletableFuture<>();
            future.completeExceptionally(new MqServerException("not support topicType="+metaData.getTopicType()));
            return future;
        }

        try {
            return topics.computeIfAbsent(topicKey, x->createTopic(metaData));
        }catch (Exception e){
            log.error("create topic error:{}",e.getMessage());
            return FutureUtil.failedFuture(e);
        }
    }


    private CompletableFuture<Topic> createTopic(TopicMetaData metaData){
        CompletableFuture<Topic> future = new CompletableFuture<>();

        metaStore.storeTopic(metaData,(v,e)->{
            if(e == null){
                future.complete(new TopicImpl(metaData,this));
            }else {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private CompletableFuture<Void> closeTopic(){

        CompletableFuture<Void> closeFuture = new CompletableFuture<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        topics.forEach((k,future)->{
            if(FutureUtil.futureSuccess(future)){
                futures.add(future.getNow(null).close());
            }
        });
        FutureUtil.waitForAll(futures)
                .thenRun(()->{
                    closeFuture.complete(null);
                }).exceptionally(e->{
            closeFuture.completeExceptionally(e);
            return null;
        });
        return closeFuture;
    }

    public MetaStore metaStore(){
        return metaStore;
    }

    public PositionOffsetStore positionOffsetStore(){
        return positionOffsetStore;
    }

    public StoreManager storeManager(){
        return storeManager;
    }

    public BrokerConfigurationData conf(){
        return conf;
    }

    public LoadBalanceManager loadBalanceManager(){
        return loadBalanceManager;
    }

    public ExecutorService pushMessageExecutor(){
        return pushMessageExecutor;
    }
    public ExecutorService readMessageExecutor(){
        return readMessageExecutor;
    }
    public AuthenticationService authenticationService(){
        return authenticationService;
    }
}
