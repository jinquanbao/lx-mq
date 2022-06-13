package com.laoxin.mq.client.api;

@FunctionalInterface
public interface MessageFilter<T> {

    //返回true:消息正常消费;返回false：消息被过滤，自动被ack
    boolean accept(Message<T> message);

}
