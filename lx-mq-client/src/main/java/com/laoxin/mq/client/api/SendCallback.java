package com.laoxin.mq.client.api;


public interface SendCallback<R> {

    void callback(R reps,Exception e);

}
