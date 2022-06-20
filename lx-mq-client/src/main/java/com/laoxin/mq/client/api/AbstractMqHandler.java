package com.laoxin.mq.client.api;

import com.laoxin.mq.client.command.BaseCommand;
import com.laoxin.mq.client.command.CommandWrapper;
import com.laoxin.mq.client.command.Commands;
import com.laoxin.mq.client.enums.CommandType;
import com.laoxin.mq.client.util.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractMqHandler extends AbstractConnectionStateHandler implements Closeable {


    protected ChannelHandlerContext ctx;
    protected SocketAddress remoteAddress;
    private ScheduledFuture<?> keepAliveTask;

    private int keepAliveTimeOut = 3;

    private int keepAliveTaskCount = 0;

    private boolean waitingPong = false;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.remoteAddress = ctx.channel().remoteAddress();
        this.ctx = ctx;
        this.keepAliveTask = ctx.executor().scheduleAtFixedRate(this::keepAliveTask, 30,
                30, TimeUnit.SECONDS);
    }

    public ChannelHandlerContext ctx() {
        return ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if(!(msg instanceof CommandWrapper)){
            log.warn("msg object is not CommandWrapper:" + msg.getClass().getName());
            ctx.fireChannelRead(msg);
            return;
        }

        CommandWrapper wrapper = (CommandWrapper) msg;

        BaseCommand cmd = BaseCommand.builder().commandType(wrapper.getCommandType()).build();
        if(wrapper.getData() != null && wrapper.getData().length > 0){
            try {
                cmd = JSONUtil.fromJson(wrapper.getData(), BaseCommand.class);
            } catch (Exception exception) {
                log.error("cmd json 格式转换异常 {}", new String(wrapper.getData()));
                return;
            }
            if(cmd == null){
                log.warn("cmd parse null: {}",new String(wrapper.getData()));
                return;
            }
        }

        commandReceived();

        if(log.isDebugEnabled() && !(CommandType.PING.name().equals(cmd.getCommandType()) || CommandType.PONG.name().equals(cmd.getCommandType()) ) ){
            log.debug("recevie cmd={}",wrapper.getCommandType());
        }

        switch (CommandType.getEnum(cmd.getCommandType())){

            case ACK:
                handleAck(cmd);
                break;

            case CLOSE_CONSUMER:
                handleCloseConsumer(cmd);
                break;

            case CLOSE_PRODUCER:
                handleCloseProducer(cmd);
                break;

            case CONNECT:
                handleConnect(cmd);
                break;
            case CONNECTED:
                handleConnected(cmd);
                break;

            case ERROR:
                handleError(cmd);
                break;


            case MESSAGE: {
                handleMessage(cmd);
                break;
            }

            case PULL: {
                handlePull(cmd);
                break;
            }

            case PULL_RECEIPT: {
                handlePullReceipt(cmd);
                break;
            }

            case PRODUCER:
                handleProducer(cmd);
                break;

            case SEND:
                handleSend(cmd);
                break;

            case SEND_ERROR:
                handleSendError(cmd);
                break;

            case SEND_RECEIPT:
                handleSendReceipt(cmd);
                break;

            case SUBSCRIBE:
                handleSubscribe(cmd);
                break;

            case SUCCESS:
                handleSuccess(cmd);
                break;

            case PRODUCER_SUCCESS:
                handleProducerSuccess(cmd);
                break;

            case UNSUBSCRIBE:
                handleUnsubscribe(cmd);
                break;

            case PING:
                handlePing(cmd);
                break;

            case PONG:
                handlePong(cmd);
                break;

            default:
                log.error("unknow commandType :{}",cmd.getCommandType());
                break;
        }
    }

    @Override
    public void close()  {
        ctx.close();
    }


    private void keepAliveTask(){
        if (!ctx.channel().isOpen()) {
            return;
        }
        if(waitingPong && keepAliveTimeOut<keepAliveTaskCount++){
            log.warn("[{}] 心跳连接超时...", ctx.channel());
            keepAliveTaskCount = 0;
            ctx.close();
        }
        waitingPong = true;
        send(Commands.newPing(),0);
    }

    protected void commandReceived(){
        waitingPong = false;
    }

    protected void handleAck(BaseCommand cmd){
        throw new UnsupportedOperationException();
    }

    protected void handleConnect(BaseCommand connect) {
        throw new UnsupportedOperationException();
    }

    protected void handleConnected(BaseCommand connected) {
        throw new UnsupportedOperationException();
    }

    protected void handleSubscribe(BaseCommand subscribe) {
        throw new UnsupportedOperationException();
    }

    protected void handleProducer(BaseCommand producer) {
        throw new UnsupportedOperationException();
    }

    protected void handleSend(BaseCommand send) {
        throw new UnsupportedOperationException();
    }

    protected void handleSendReceipt(BaseCommand sendReceipt) {
        throw new UnsupportedOperationException();
    }

    protected void handleSendError(BaseCommand sendError) {
        throw new UnsupportedOperationException();
    }

    protected void handleMessage(BaseCommand cmdMessage) {
        throw new UnsupportedOperationException();
    }

    protected void handlePull(BaseCommand cmdMessage) {
        throw new UnsupportedOperationException();
    }

    protected void handlePullReceipt(BaseCommand cmdMessage) {
        throw new UnsupportedOperationException();
    }




    protected void handleUnsubscribe(BaseCommand unsubscribe) {
        throw new UnsupportedOperationException();
    }

    protected void handleSuccess(BaseCommand success) {
        throw new UnsupportedOperationException();
    }

    protected void handleProducerSuccess(BaseCommand success) {
        throw new UnsupportedOperationException();
    }

    protected void handleError(BaseCommand error) {
        throw new UnsupportedOperationException();
    }

    protected void handleCloseProducer(BaseCommand closeProducer) {
        throw new UnsupportedOperationException();
    }

    protected void handleCloseConsumer(BaseCommand closeConsumer) {
        throw new UnsupportedOperationException();
    }

    protected void handlePing(BaseCommand ping) {
        send(Commands.newPong(),0);
    }

    protected void handlePong(BaseCommand pong) {
    }


    public void send(BaseCommand cmd, long requestId) {
        send(cmd,requestId,null);
    }

    public void send(BaseCommand cmd, long requestId, String authClientId) {
        if(requestId>0){
            cmd.requestId(requestId);
        }
        if(authClientId != null){
            cmd.authClientId(authClientId);
        }
        ctx.writeAndFlush(cmd.toCommandWrapper());
    }


}
