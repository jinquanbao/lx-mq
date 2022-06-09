package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.SendCallback;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class DefaultSendCallback<T> implements SendCallback<T> {

    private final CompletableFuture<T> futrue;

    public DefaultSendCallback(CompletableFuture<T> futrue){
        this.futrue = futrue;
    }

    @Override
    public void callback(T reps, Exception e) {
        if(futrue.isDone()){
            log.info("send wait is done, reps={}",reps);
            return;
        }
        if(log.isTraceEnabled()){
            log.trace("send receive call back, reps={}",reps);
        }
        if (e != null) {
            futrue.completeExceptionally(e);
        } else {
            futrue.complete(reps);
        }
    }
}
