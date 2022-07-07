package com.laoxin.mq.client.stats;

import java.util.concurrent.atomic.LongAdder;

public interface ProducerStats {
    //连接时间
    long getConnectedTimestamp();
    //断线时间
    long getDisconnectedTimestamp();
    //生产者名称
    String getProducerName();
    //发送速率
    double getMsgRateOut();
    //上一次统计到现在发送到主题的消息总数
    long getMsgOutCounter();
    //上一次统计到现在发送到主题的消息总数
    long getMsgOutFailedCounter();
    //发送到主题的消息总数
    LongAdder getTotalMsgOutCounter();
    //发送到主题的消息失败总数
    LongAdder getTotalMsgOutFailedCounter();
    //最近一次生产消息时间
    long getLastMsgOutTimestamp();
    //连接状态
    int getConnectionStatus();
    //异常信息
    Throwable getException();
    //发生异常的时间
    long getExceptionTimestamp();
}
