package com.laoxin.mq.broker.stats;

import java.util.concurrent.atomic.LongAdder;

public interface ProducerStats {
    //生产者名称
    String getProducerName();
    //租户ID
    long getTenantId();
    //发送速率
    double getMsgRateIn();
    //上一次统计到现在发送到主题的消息总数
    long getMsgInCounter();
    //发送到主题的消息总数
    LongAdder getTotalMsgInCounter();
    //最近一次生产消息时间
    long getLastMsgInTimestamp();
    //异常信息
    Throwable getException();
    //发生异常的时间
    long getExceptionTimestamp();
}
