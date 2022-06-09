package com.laoxin.mq.client.api;

@FunctionalInterface
public interface MessageFilter<T> {

    boolean accept(Message<T> message);

}
