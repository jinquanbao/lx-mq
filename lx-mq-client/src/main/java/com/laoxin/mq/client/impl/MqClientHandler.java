package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.AbstractMqHandler;
import com.laoxin.mq.client.api.SendCallback;
import com.laoxin.mq.client.command.BaseCommand;
import com.laoxin.mq.client.command.CommandError;
import com.laoxin.mq.client.command.CommandMessage;
import com.laoxin.mq.client.command.CommandSendReceipt;
import com.laoxin.mq.client.conf.ClientConfigurationData;
import com.laoxin.mq.client.enums.CommandType;
import com.laoxin.mq.client.enums.ConnectionState;
import com.laoxin.mq.client.exception.MqClientException;
import com.laoxin.mq.client.util.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MqClientHandler extends AbstractMqHandler {

    protected MqClientImpl client;

    private final CompletableFuture<Void> connectionFuture;
    //同一连接通道可能有多个生产者消息者
    private final ConcurrentHashMap<Long,ProducerImpl> producers;
    private final ConcurrentHashMap<Long,ConsumerImpl> consumers;

    private final ClientConfigurationData conf;
    private final SendOpsAccept accept;
    private final long acceptTimeOut = 30*1000;


    public MqClientHandler(MqClientImpl client) {
        super();
        this.client = client;
        this.conf = client.getClientConfiguration();
        this.accept = client.getSendOpsAccept();
        connectionFuture = new CompletableFuture();
        producers = new ConcurrentHashMap<>();
        consumers = new ConcurrentHashMap<>();
    }

    public CompletableFuture<Void> connectionFuture(){
        return connectionFuture;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("{} Connected to broker", ctx.channel());
        // 连接，鉴权
        ctx.writeAndFlush(BaseCommand.builder()
                .commandType(CommandType.CONNECT.name())
                .build()
                .authClientId(conf.getAuthClientId())
                .toCommandWrapper()
        ).addListener(future -> {
            if (future.isSuccess()) {
                if (log.isDebugEnabled()) {
                    log.debug("发送连接命令成功: {}", future.isSuccess());
                }
            } else {
                log.warn("发送连接命令失败", future.cause());
                state = ConnectionState.Closing;
                ctx.close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("client与broker连接中断:{}", ctx.channel());

        MqClientException e = new MqClientException(
                "client与broker连接中断: " + ctx.channel().remoteAddress());

        state = ConnectionState.Closed;

        //生产者，消费者是否需要重连
        producers.forEach((k,v)->v.reconnect(e));
        consumers.forEach((k,v)->v.reconnect(e));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("{} Exception caught: {}", ctx.channel(), cause.getMessage(), cause);
        state = ConnectionState.Closing;
        ctx.close();
    }


    @Override
    protected void handleConnected(BaseCommand connected) {
        log.info("通道已连接：{}", ctx.channel());
        connectionFuture.complete(null);
        state = ConnectionState.Connected;
    }

    @Override
    protected void handleSendReceipt(BaseCommand sendReceipt) {

        checkConnected();

        CommandSendReceipt receipt = JSONUtil.fromJson(sendReceipt.getBody(), CommandSendReceipt.class);
        if(receipt == null){
            log.debug("receipt msg body is null");
            return;
        }

        ProducerImpl producer = producers.get(receipt.getProducerId());
        if(producer == null){
            log.warn("producer is closed in client, producerId={}",receipt.getProducerId());
            return;
        }

        //ack send
        producer.ackReceived(receipt,sendReceipt.getRequestId());

    }

    //处理broker 推送的消息
    @Override
    protected void handleMessage(BaseCommand baseCommand) {

        checkConnected();

        CommandMessage commandMessage = JSONUtil.fromJson(baseCommand.getBody(), CommandMessage.class);
        if(commandMessage == null){
            log.debug("msg body is null");
            return;
        }

        ConsumerImpl consumer = consumers.get(commandMessage.getConsumerId());
        if(consumer == null){
            log.warn("consumer is closed in client, consumerId={}",commandMessage.getConsumerId());
            return;
        }

        consumer.messageReceived(commandMessage,baseCommand.getHeader());
    }

    @Override
    protected void handlePullReceipt(BaseCommand cmd) {

        checkConnected();

        CommandMessage commandMessage = JSONUtil.fromJson(cmd.getBody(), CommandMessage.class);

        accept.accept(cmd.getRequestId(),commandMessage,null);

    }

    @Override
    protected void handleSuccess(BaseCommand success) {
        if (log.isDebugEnabled()) {
            log.debug("{} Received success response from server: {}", ctx.channel(), success.getRequestId());
        }
        accept.accept(success.getRequestId(),success,null);
    }

    @Override
    protected void handleProducerSuccess(BaseCommand success) {
        if (log.isDebugEnabled()) {
            log.debug("{} 生产者注册成功: requertId={} ", ctx.channel(),
                    success.getRequestId());
        }
        accept.accept(success.getRequestId(),success,null);
    }

    @Override
    protected void handleSendError(BaseCommand cmd) {
        log.warn("broker 接收发送的消息错误: {}", ctx.channel(), cmd);
        accept.accept(cmd.getRequestId(),cmd,new MqClientException("broker ack send error"));
        ctx.close();
    }

    @Override
    protected void handleError(BaseCommand cmd) {

        CommandError error = JSONUtil.fromJson(cmd.getBody(),CommandError.class);

        log.warn("broker处理消息错误,ch:{},requestId={},error={}", ctx.channel(),cmd.getRequestId(),error.getMsg());

        accept.accept(cmd.getRequestId(),cmd,new MqClientException("broker handle error:"+error.getMsg()));

    }


    void registerConsumer(final long consumerId, final ConsumerImpl consumer) {
        consumers.put(consumerId, consumer);
    }

    void registerProducer(final long producerId, final ProducerImpl producer) {
        producers.put(producerId, producer);
    }


    void removeProducer(final long producerId) {
        producers.remove(producerId);
    }

    void removeConsumer(final long consumerId) {
        consumers.remove(consumerId);
    }

    public CompletableFuture<BaseCommand> sendAsync(BaseCommand cmd) {
        return sendAsync(cmd,client.newRequestId(),BaseCommand.class);
    }

    public CompletableFuture<BaseCommand> sendAsync(BaseCommand cmd, long requestId) {
        return sendAsync(cmd,requestId,BaseCommand.class);
    }

    public <T> CompletableFuture<T> sendAsync(BaseCommand cmd, long requestId,Class<T> clazz) {
        CompletableFuture<T> future = new CompletableFuture<>();
        sendAsync(cmd,requestId,future,new DefaultSendCallback(future));
        return future;
    }

    public void sendAsync(BaseCommand cmd, long requestId,CompletableFuture<?> future, SendCallback callback) {

        if(requestId>0){
            cmd.requestId(requestId);
        }
        if(conf.getAuthClientId() != null){
            cmd.authClientId(conf.getAuthClientId());
        }

        long createAt = System.currentTimeMillis();

        if(log.isTraceEnabled()){
            log.trace("registerSendOps, requestId={},createAt={}",requestId,createAt);
        }

        accept.registerSendOps(requestId,SendOps.builder()
                .createAt(createAt)
                .expiredAt(createAt+acceptTimeOut)
                .commandType(cmd.getCommandType())
                .callback(callback)
                .build());

        ctx.writeAndFlush(cmd.toCommandWrapper()).addListener(writeFuture -> {
            if (!writeFuture.isSuccess()) {
                log.warn("{} 消息发送至broker失败: {}", ctx.channel(), writeFuture.cause().getMessage());
                accept.removeSendOps(requestId);
                future.completeExceptionally(writeFuture.cause());
            }
        });
    }
}
