package com.laoxin.mq.client.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CommandType {
    //client发送连接命令
    CONNECT("CONNECT",1),
    //broker回复已连接
    CONNECTED("CONNECTED",2),
    //client发送消费者订阅
    SUBSCRIBE("SUBSCRIBE",3),
    //client发送生产者注册
    PRODUCER("PRODUCER",4),
    //broker 回复生产者注册成功
    PRODUCER_SUCCESS("PRODUCER_SUCCESS",5),
    //client 生产者发送消息
    SEND("SEND",6),
    //broker 回复生产者发送成功的消息
    SEND_RECEIPT("SEND_RECEIPT",7),
    //broker 回复生产者发送失败的消息
    SEND_ERROR("SEND_ERROR",8),

    //broker 主动推送消息
    MESSAGE("MESSAGE",9),
    //client 消费者主动ack
    ACK("ACK",10),
    //client 消费者主动拉取消息
    PULL("PULL",11),
    //broker 回复消费者拉取的消息
    PULL_RECEIPT("PULL_RECEIPT",12),

    //消费者取消订阅
    UNSUBSCRIBE("UNSUBSCRIBE",13),

    //成功回复
    SUCCESS("SUCCESS",14),
    //失败回复
    ERROR("ERROR",15),

    CLOSE_PRODUCER("CLOSE_PRODUCER",16),
    CLOSE_CONSUMER("CLOSE_CONSUMER",17),



    PING("PING",18),
    PONG("PONG",19),

    SEEK("SEEK",20),

    ;

    private String name;
    private int type;



    public static CommandType getEnum(String name){
        return Arrays.stream(CommandType.values())
                .filter(x->x.name().equals(name))
                .findAny()
                .orElse(null);
    }

    public static CommandType getEnum(int type){
        return Arrays.stream(CommandType.values())
                .filter(x->type == x.getType())
                .findAny()
                .orElse(null);
    }
}
