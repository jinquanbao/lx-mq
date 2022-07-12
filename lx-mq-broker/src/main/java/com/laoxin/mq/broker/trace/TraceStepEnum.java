package com.laoxin.mq.broker.trace;

import java.util.Arrays;

public enum TraceStepEnum {

    msg_in, //生产者发送的消息已接收到
    msg_stored, //生产者发送的消息已存储
    msg_outed,  //消息已推送给消费者
    msg_acked,  //消息已经被确认消费
    ;



    public static TraceStepEnum getEnum(String step){
        return Arrays.stream(TraceStepEnum.values())
                .filter(x->x.name().equals(step))
                .findAny()
                .orElse(null);
    }
}
