package com.laoxin.mq.client.enums;

import java.util.Arrays;

public enum CommandType {
    //client发送连接命令
    CONNECT,
    //broker回复已连接
    CONNECTED,
    //client发送消费者订阅
    SUBSCRIBE,
    //client发送生产者注册
    PRODUCER,
    //broker 回复生产者注册成功
    PRODUCER_SUCCESS,
    //client 生产者发送消息
    SEND,
    //broker 回复生产者发送成功的消息
    SEND_RECEIPT,
    //broker 回复生产者发送失败的消息
    SEND_ERROR,

    //broker 主动推送消息
    MESSAGE,
    //client 消费者主动ack
    ACK,
    //client 消费者主动拉取消息
    PULL,
    //broker 回复消费者拉取的消息
    PULL_RECEIPT,

    //消费者取消订阅
    UNSUBSCRIBE,

    //成功回复
    SUCCESS,
    //失败回复
    ERROR,

    CLOSE_PRODUCER,
    CLOSE_CONSUMER,



    PING,
    PONG,

    ;

    public static CommandType getEnum(String commandType){
        return Arrays.stream(CommandType.values())
                .filter(x->x.name().equals(commandType))
                .findAny()
                .orElse(null);
    }
}
