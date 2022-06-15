package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.ClientConnection;
import com.laoxin.mq.client.exception.MqClientException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public abstract class DefaultClientConnection extends HandlerState implements ClientConnection {

    protected final MqClientImpl client;

    protected final String topic;

    protected final AtomicReference<MqClientHandler> ch;


    public DefaultClientConnection(MqClientImpl client, String topic){
        this.client = client;
        this.topic = topic;
        this.ch = new AtomicReference<>();
    }

    protected void openConnection(){
        if (ch.get() != null) {
            log.warn("客户端连接已设置,topic=[{}],hander=[{}] ", topic, getHandlerName());
            return;
        }
        try {
            client.getConnection()
                    .thenAccept(this::connectionOpened)
                    .exceptionally(this::connectionError)
            ;
        }catch (Exception e){
            log.warn("[{}] [{}] 获取连接异常 connection: ", topic, getHandlerName(), e);
            reconnect(e);
        }

    }

    protected void reconnect(Throwable exception) {
        if (!reconnectionIfNeed()) {
            log.info("[{}] [{}] Ignoring reconnection request (state: {})", topic, getHandlerName(), getState());
            return;
        }
        ch.set(null);
        //重置连接
        super.setState(State.Connecting);
        long delayMs = 2000;
        client.timer().newTimeout(timeout -> {
            log.info("连接通道关闭，尝试重建连接...,topic={},handler={}", topic, getHandlerName());
            openConnection();
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    private Void connectionError(Throwable exception) {
        log.warn("[{}] [{}] 连接到broker异常: {}", topic, getHandlerName(), exception.getMessage());

        connectionFailed(MqClientException.translateException(exception));

        this.reconnect(exception);
        return null;
    }

    protected boolean reconnectionIfNeed() {
        State state = this.getState();
        switch (state) {
            case Uninitialized:
            case Connecting:
            case Ready:
                return true;
            case Closing:
            case Closed:
            case Failed:
                return false;
        }
        return false;
    }

    protected boolean isConnected() {
        return ch.get() != null && (getState() == State.Ready);
    }

    @Override
    public void connectionOpened(MqClientHandler handler) {
        ch.set(handler);
    }

    protected MqClientHandler ch(){
        return ch.get();
    }
}
