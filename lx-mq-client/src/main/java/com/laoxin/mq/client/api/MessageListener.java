package com.laoxin.mq.client.api;

@FunctionalInterface
public interface MessageListener<T> {

    void onMessage(Consumer consumer,Message<T> msg);

}
