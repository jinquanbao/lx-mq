package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.authentication.CredentialsAuthenticationDTO;
import com.laoxin.mq.broker.authentication.UserAuthContext;
import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.client.api.AbstractMqHandler;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.command.*;
import com.laoxin.mq.client.enums.CommandType;
import com.laoxin.mq.client.enums.ConnectionState;
import com.laoxin.mq.client.enums.ResultErrorEnum;
import com.laoxin.mq.client.util.FutureUtil;
import com.laoxin.mq.client.util.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class MqServerHandler extends AbstractMqHandler {

    private final BrokerService service;
    private UserAuthContext authContext;
    private final ConcurrentHashMap<ProducerKey,CompletableFuture<Producer>> producers;
    private final ConcurrentHashMap<ConsumerKey,CompletableFuture<Consumer>> consumers;

    public MqServerHandler(BrokerService service){
        this.service = service;
        this.consumers = new ConcurrentHashMap<>();
        this.producers = new ConcurrentHashMap<>();
        this.authContext = new UserAuthContext();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("New connection from {}", remoteAddress);
        this.ctx = ctx;
        state = ConnectionState.Connecting;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("Closed connection from {}", remoteAddress);
        producers.values().forEach((producerFuture) -> {
            if(FutureUtil.futureSuccess(producerFuture)){
                Producer producer = producerFuture.getNow(null);
                try {
                    producer.close();
                } catch (Exception e) {
                    log.warn("producer {} was already closed: {}", producer, e.getMessage(), e);
                }
            }
        });
        consumers.values().forEach((consumerFuture) -> {
            if(FutureUtil.futureSuccess(consumerFuture)){
                Consumer consumer = consumerFuture.getNow(null);
                try {
                    consumer.close();
                } catch (Exception e) {
                    log.warn("Consumer {} was already closed: {}", consumer, e.getMessage(), e);
                }
            }
        });
        state = ConnectionState.Closed;
    }

    private long checkTenantId(long tenantId){

        if(tenantId >0 ){

            if(tenantId != authContext.getTenantId() && !authContext.scopeAll()){
                log.error("handle reject for tenantId overreach currentTenantId={},authTenantId={}",authContext.getTenantId());
                throw new IllegalArgumentException("handle reject for tenantId overreach");
            }

            return tenantId;

        }else {
            return authContext.getTenantId();
        }

    }

    @Override
    protected void handleConnect(BaseCommand connect) {

        final String clientId = connect.getAuthClientId();

        try {
            //鉴权
            authContext = service.authenticationService().authenticate(CredentialsAuthenticationDTO
                    .builder()
                    .clientId(clientId)
                    .build());

            log.info("connect success scope={},tenantId={}",authContext.getClientId(),authContext.getTenantId());
        }catch (Exception e){
            send(Commands.newError(ResultErrorEnum.AUTH_FAILED.getCode(),e.getMessage()),connect.getRequestId());
            closeIfNoneProducerAndConsumer();
            return;
        }

        send(BaseCommand.builder()
                .commandType(CommandType.CONNECTED.name())
                .build(),0);

        state = ConnectionState.Connected;
    }

    @Override
    protected void handleSubscribe(final BaseCommand cmd) {

        //校验连接状态
        checkConnected();

        CommandSubscribe subscribe = JSONUtil.fromJson(cmd.getBody(),CommandSubscribe.class);
        //校验租户是否越权
        long tenantId = checkTenantId(subscribe.getTenantId());

        ConsumerKey consumerKey = ConsumerKey.builder()
                .consumerId(subscribe.getConsumerId())
                .tenantId(tenantId)
                .build();

        CompletableFuture<Consumer> consumerFuture = new CompletableFuture<>();
        //消费者是否已在当前连接订阅
        CompletableFuture<Consumer> existFuture = consumers.putIfAbsent(consumerKey,consumerFuture);
        if(existFuture != null){
            if(existFuture.isDone() && !existFuture.isCompletedExceptionally()){
                final Consumer consumer = existFuture.getNow(null);
                log.info("consumer=[{}] alreay created",consumer);
                send(Commands.newSuccess(),cmd.getRequestId());
            }else {
                log.warn("consumer=[{}] is already creating on the connection", consumerKey);
                send(Commands.newError("1","consumer is already creating on the connection"),cmd.getRequestId());
            }
            return;
        }

        TopicMetaData topicMetaData = TopicMetaData.builder()
                .tenantId(tenantId)
                .topicName(subscribe.getTopic())
                .topicType(subscribe.getTopicType())
                .build();

        service.getTopic(topicMetaData)
                .thenCompose(topic ->
                    topic.subscribe(this,consumerKey,
                            SubscriptionMetaData.builder()
                                    .filterExpression(subscribe.getFilterExpression())
                                    .subscriptionProperties(subscribe.getSubscriptionProperties())
                                    .subscriptionName(subscribe.getSubscription())
                                    .dependencyOnSubscription(subscribe.getDependencyOnSubscription())
                                    .subscriptionType(subscribe.getSubscribeType())
                                    .ackTimeOut(subscribe.getAckTimeOut())
                                    .enablePush(subscribe.isEnablePush())
                                    .consumer(SubscriptionConsumer.builder()
                                            .address(remoteAddress.toString())
                                            .consumerName(subscribe.getConsumerName())
                                            .build())
                                    .build()
                            )
                ).thenAccept(consumer->{
                    //订阅成功
                    if(consumerFuture.complete(consumer)){
                        log.info("客户端[{}]订阅主题:[{}]成功，订阅名称:[{}]", remoteAddress, subscribe.getTopic(),
                                subscribe.getSubscription());
                        send(Commands.newSuccess(),cmd.getRequestId());
                    }else {
                        consumer.close();
                        consumers.remove(consumerKey);
                    }
                }).exceptionally(e->{
                    log.error("消费者[{}]订阅主题[{}]失败 ,订阅名称:[{}],errMsg={}",remoteAddress, subscribe.getTopic(),subscribe.getSubscription(),e.getMessage());
                    //订阅失败
                    consumers.remove(consumerKey, consumerFuture);
                    if (consumerFuture.completeExceptionally(e)) {
                        final MqServerException exception = MqServerException.translateException(e);
                        send(Commands.newError(exception.getCode(),
                                exception.getMessage()),
                                cmd.getRequestId());
                    }
                    return null;
                });


    }

    @Override
    protected void handleProducer(final BaseCommand cmd) {

        //校验连接状态
        checkConnected();

        CommandCreateProducer producer = JSONUtil.fromJson(cmd.getBody(), CommandCreateProducer.class);

        //校验租户是否越权
        checkTenantId(producer.getTenantId());

        ProducerKey producerKey = ProducerKey.builder()
                .producerId(producer.getProducerId())
                .tenantId(producer.getTenantId())
                .build();

        CompletableFuture<Producer> future = new CompletableFuture<>();
        //生产者是否已在当前连接注册
        CompletableFuture<Producer> existFuture = producers.putIfAbsent(producerKey,future);
        if(existFuture != null){
            if(existFuture.isDone() && !existFuture.isCompletedExceptionally()){
                final Producer p = existFuture.getNow(null);
                log.info("producer=[{}] alreay created",p);
                send(Commands.newSuccess(),cmd.getRequestId());
            }else {
                log.warn("producer=[{}] is already creating on the connection", producerKey);
                send(Commands.newError("1","producer is already creating on the connection"),cmd.getRequestId());
            }
            return;
        }

        TopicMetaData topicMetaData = TopicMetaData.builder()
                .tenantId(producer.getTenantId())
                .topicName(producer.getTopic())
                .topicType(producer.getTopicType())
                .build();

        service.getTopic(topicMetaData)
                .thenAccept(topic -> {
                    //注册生产者
                    Producer p = new Producer(this,topic,producerKey,producer.getProducerName());
                    topic.registerProducer(p);
                    if(isConnected()){
                        if(future.complete(p)){
                            log.info("客户端[{}]生产者[{}]注册成功，绑定topic[{}]",remoteAddress,p,producer.getTopic());
                            send(Commands.newProducerSuccess(producer.getProducerName()),cmd.getRequestId());
                        }else {
                            p.close();
                        }
                    }else {
                        future.completeExceptionally(new MqServerException("conn is closed when producer created"));
                        p.close();
                        log.warn("conn is closed when producer created,producer={}，address={}",p,remoteAddress);
                    }
                })
                .exceptionally(e->{
                    //注册失败
                    producers.remove(producerKey);
                    log.error("生产者注册失败 ,producer={},address={},errMsg={}",producerKey,remoteAddress,e.getMessage());
                    if(future.completeExceptionally(e)){
                        send(Commands.newError("1",e.getMessage()),cmd.getRequestId());
                    }
                    return null;
                });
    }

    @Override
    protected void handleSend(BaseCommand cmd) {
        //校验连接状态
        checkConnected();

        CommandSend send = JSONUtil.fromJson(cmd.getBody(), CommandSend.class);
        //校验数据权限
        checkTenantId(send.getTenantId());

        ProducerKey producerKey = ProducerKey.builder()
                .producerId(send.getProducerId())
                .tenantId(send.getTenantId())
                .build();
        //校验注册信息
        final CompletableFuture<Producer> future = producers.get(producerKey);
        if(future == null || !future.isDone() || future.isCompletedExceptionally()){
            log.warn("producer is not register on the conn or already closed ,producer={},address={}",producerKey,remoteAddress);
            send(Commands.newError("1","producer is not register on the conn or already closed"),cmd.getRequestId());
        }
        //处理client发送的信息
        final Producer producer = future.getNow(null);
        producer.publishMessage(send.getMessage())
        .thenAccept(entryId -> {
            if(log.isDebugEnabled()){
                log.debug("broker handle send success,producer={},address={},requestId={}",producer,remoteAddress,cmd.getRequestId());
            }
            send(Commands.newSendReceipt(send.getProducerId(),send.getTenantId(),entryId),cmd.getRequestId());
        }).exceptionally(e->{
            log.error("broker handle send error,producer={},address={},requestId={},errMsg={}",producer,remoteAddress,cmd.getRequestId(),e.getMessage(),e);
            send(Commands.newError("1",e.getMessage()),cmd.getRequestId());
            return null;
        });

    }


    @Override
    protected void handleAck(BaseCommand cmd) {

        checkConnected();

        CommandAck ack = JSONUtil.fromJson(cmd.getBody(), CommandAck.class);

        checkTenantId(ack.getTenantId());

        final ConsumerKey consumerKey = ConsumerKey.builder().tenantId(ack.getTenantId()).consumerId(ack.getConsumerId()).build();
        CompletableFuture<Consumer> future = consumers.get(consumerKey);

        if(FutureUtil.futureSuccess(future)){
            future.getNow(null)
                    .ackAsync(ack)
                    .handle((v,e)->{
                        if(e != null){
                            log.error("broker handle send error,consumer={},address={},requestId={},errMsg={}",consumerKey,remoteAddress,cmd.getRequestId(),e.getMessage(),e);
                            send(Commands.newError("1",e.getMessage()),cmd.getRequestId());
                        }else {
                            if(log.isDebugEnabled()){
                                log.debug("broker handle ack success,ack={},address={},requestId={}",ack,remoteAddress,cmd.getRequestId());
                            }
                            send(Commands.newSuccess(),cmd.getRequestId());
                        }
                        return null;
                    });
        }else {
            log.warn("consumer future creating on connection,consumer={},address={}",consumerKey,remoteAddress);
        }
    }

    @Override
    protected void handleSeek(BaseCommand cmd) {
        checkConnected();

        CommandSeek seek = JSONUtil.fromJson(cmd.getBody(), CommandSeek.class);

        checkTenantId(seek.getTenantId());

        final ConsumerKey consumerKey = ConsumerKey.builder().tenantId(seek.getTenantId()).consumerId(seek.getConsumerId()).build();
        CompletableFuture<Consumer> future = consumers.get(consumerKey);

        if(FutureUtil.futureSuccess(future)){
            future.getNow(null)
                    .seek(seek)
                    .handle((v,e)->{
                        if(e != null){
                            log.error("broker handle seek error,consumer={},address={},requestId={},errMsg={}",consumerKey,remoteAddress,cmd.getRequestId(),e.getMessage(),e);
                            send(Commands.newError("1",e.getMessage()),cmd.getRequestId());
                        }else {
                            log.info("broker handle seek success,seek={},address={},requestId={}",seek,remoteAddress,cmd.getRequestId());
                            send(Commands.newSuccess(),cmd.getRequestId());
                        }
                        return null;
                    });
        }else {
            log.warn("consumer future creating on connection,consumer={},address={}",consumerKey,remoteAddress);
        }
    }

    @Override
    protected void handlePull(BaseCommand cmd) {

        //校验连接状态
        checkConnected();

        CommandPull pull = JSONUtil.fromJson(cmd.getBody(), CommandPull.class);

        //校验数据权限
        long tenantId = checkTenantId(pull.getTenantId());

        List<Message> result = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();
        List<CompletableFuture<List<Message>>> futures = new ArrayList<>();

        final ConsumerKey consumerKey = ConsumerKey.builder().tenantId(tenantId).consumerId(pull.getConsumerId()).build();
        CompletableFuture<Consumer> future = consumers.get(consumerKey);
        //处理拉取请求
        if(FutureUtil.futureSuccess(future)){
            pull.setTenantId(tenantId);
            futures.add(future.getNow(null)
                    .pull(pull));
        }else {
            log.warn("consumer future creating on connection,consumer={},address={}",consumerKey,remoteAddress);
            return;
        }

        futures.forEach(x->x.whenComplete((messages, e) -> {
            if(e != null){
                errors.add(e);
            }else if(messages != null && !messages.isEmpty()){
                result.addAll(messages);
            }
        }));

        if(errors.isEmpty()){
            if(log.isDebugEnabled()){
                log.debug("handle consumer pull success,topic={},subscription,tenantId={},address={}",pull.getTopic(),pull.getSubscription(),tenantId,remoteAddress);
            }
            send(Commands.newPullReceipt(pull.getConsumerId(),JSONUtil.toJson(result)),cmd.getRequestId());
            future.getNow(null).pullSendSuccess(result);
        }else if(!errors.isEmpty()){
            final String errMsg = errors.stream().map(x -> x.getMessage()).collect(Collectors.joining());
            log.error("handle consumer pull error,topic={},subscription,tenantId={},address={},errMsg={}",pull.getTopic(),pull.getSubscription(),tenantId,remoteAddress,errMsg);
            future.getNow(null).pullSendFailed(errors.get(0));
            send(Commands.newError("1",errMsg),cmd.getRequestId());
        }

    }


    @Override
    protected void handleUnsubscribe(BaseCommand cmd) {

        checkConnected();

        CommandUnSubscribe unSubscribe = JSONUtil.fromJson(cmd.getBody(), CommandUnSubscribe.class);

        List<Long> activeTenantIds = Arrays.asList(checkTenantId(unSubscribe.getTenantId()));

        CompletableFuture wait = new CompletableFuture();
        boolean consumerNotFound = true;
        for(long tenantId:activeTenantIds){

            ConsumerKey consumerKey = ConsumerKey.builder().tenantId(tenantId).consumerId(unSubscribe.getConsumerId()).build();
            final CompletableFuture<Consumer> future = consumers.get(consumerKey);
            if(FutureUtil.futureSuccess(future)){
                consumerNotFound = false;
                future.getNow(null)
                        .unsubscribe()
                        .thenRun(()->{
                            log.info("consumer unSubscribe success, consumerKey={}",consumerKey);
                    }).exceptionally(e->{
                        log.error("consumer unSubscribe error,consumerKey={},errMsg={}",consumerKey,e.getMessage());
                        wait.completeExceptionally(e);
                        return null;
                    });
            }
        }

        if(wait.isCompletedExceptionally() || consumerNotFound){
            log.info("consumerNotFound or consumer unSubscribe error, activeTenantIds={},topic={},subscribe={}",activeTenantIds,unSubscribe.getTopic(),unSubscribe.getSubscribe());
            send(Commands.newError("1","consumerNotFound or consumer unSubscribe error"),cmd.getRequestId());
        }else {
            send(Commands.newSuccess(),cmd.getRequestId());
        }
    }

    @Override
    protected void handleCloseProducer(BaseCommand cmd) {

        checkConnected();

        CommandCloseProducer close = JSONUtil.fromJson(cmd.getBody(), CommandCloseProducer.class);

        ProducerKey producerKey = ProducerKey.builder().producerId(close.getProducerId()).tenantId(close.getTenantId()).build();

        final CompletableFuture<Producer> future = producers.get(producerKey);
        if(future == null){
            log.warn("producer is not register on the conn,producer={},address={}",producerKey,remoteAddress);
            send(Commands.newError("1","producer is not register on the conn"),cmd.getRequestId());
        }

        if(!future.isDone()){
            //终止正在注册的生产者
            future.completeExceptionally(new MqServerException("client send Closed producer"));
        }

        try {
            final Producer producer = future.getNow(null);
            if(producer != null){
                producer.close();
            }
            producers.remove(producerKey);
            send(Commands.newSuccess(),cmd.getRequestId());
            log.info("producer close success, producer={}",producer);
        }catch (Exception e){
            log.warn("close producer error,topic={},producerKey={},address={},errMsg={} ",close.getTopic(),producerKey,remoteAddress,e.getMessage());
            send(Commands.newError("1",e.getMessage()),cmd.getRequestId());
        }

    }

    @Override
    protected void handleCloseConsumer(BaseCommand cmd) {

        checkConnected();

        CommandCloseConsumer close = JSONUtil.fromJson(cmd.getBody(), CommandCloseConsumer.class);

        Set<Long> activeTenantIds = new HashSet<>();
        activeTenantIds.add(checkTenantId(close.getTenantId()));

        try {
            activeTenantIds.forEach(tenantId->{
                ConsumerKey consumerKey = ConsumerKey.builder().tenantId(tenantId).consumerId(close.getConsumerId()).build();
                final CompletableFuture<Consumer> future = consumers.get(consumerKey);
                if(future != null){
                    if(!future.isDone()){
                        //终止正在订阅的消费者
                        future.completeExceptionally(new MqServerException("client send Closed consumer"));
                    }
                    final Consumer consumer = future.getNow(null);
                    if(consumer != null){
                        consumer.close();
                    }
                    consumers.remove(consumerKey);
                }
            });
            //关闭消费者成功
            send(Commands.newSuccess(),cmd.getRequestId());
            log.info("close consumer success,topic={},subscription={},address={} ",close.getTopic(),close.getSubscription(),remoteAddress);

        }catch (Exception e){
            log.warn("close consumer error,topic={},subscription={},address={},errMsg={} ",close.getTopic(),close.getSubscription(),remoteAddress,e.getMessage());
            send(Commands.newError("1",e.getMessage()),cmd.getRequestId());
        }

    }

    public void closeConsumer(Consumer consumer) {
       close();
    }

    public void closeProducer(Producer producer) {
       close();
    }

    void closeIfNoneProducerAndConsumer(){
        if(consumers.isEmpty() && producers.isEmpty()){
            super.close();
        }
    }

    void removeConsumer(Consumer consumer){

        if(log.isDebugEnabled()){
            log.debug("[{}] Removed consumer: consumerKey={}, consumer={}", remoteAddress, consumer.getConsumerKey(), consumer);
        }

        CompletableFuture<Consumer> future = consumers.get(consumer.getConsumerKey());

        if (future != null) {
            future.whenComplete((consumer2, exception) -> {
                if (exception != null || consumer2 == consumer) {
                    consumers.remove(consumer.getConsumerKey(), future);
                }
            });
        }
    }

    void removeProducer(Producer producer){

        if(log.isDebugEnabled()){
            log.debug("[{}] Removed producer: producerKey={}, producer={}", remoteAddress, producer.getProducerKey(), producer);
        }

        CompletableFuture<Producer> future = producers.get(producer.getProducerKey());

        if (future != null) {
            future.whenComplete((producer2, exception) -> {
                if (exception != null || producer2 == producer) {
                    consumers.remove(producer.getProducerKey(), future);
                }
            });
        }
    }

    SocketAddress clientAddress(){
        return remoteAddress;
    }
}
