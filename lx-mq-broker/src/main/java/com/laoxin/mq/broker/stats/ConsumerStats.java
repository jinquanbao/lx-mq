package com.laoxin.mq.broker.stats;

import java.util.concurrent.atomic.LongAdder;

public interface ConsumerStats {
    //消费者名称
    String getConsumerName();
    //租户id
    long getTenantId();
    //连接地址
    String getAddress();
    //消息推送速率 msg/s
    double getMsgRateOut();
    //消息出口数量
    long getMsgOutCounter();
    //发送给消费者的消息总数
    LongAdder getTotalMsgOutCounter();
    //连接时间
    long getConnectedTimestamp();
    //最近一次ack时间
    long getLastAckedTimestamp();
    //最近一次消费时间
    long getLastMsgOutTimestamp();
    //最近一次ack的位移
    long getLastAckedPosition();
    //最近一次发送给消费者消息位移
    long getLastMsgOutPosition();
    //异常信息
    Throwable getException();
    //发生异常的时间
    long getExceptionTimestamp();
}
