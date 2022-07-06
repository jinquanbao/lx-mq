package com.laoxin.mq.broker.stats;

import java.util.List;

public interface SubscriptionStats {

    //订阅名称
    String getSubscriptionName();
    //租户ID
    long getTenantId();
    //订阅连接时间
    long getConnectedTimestamp();
    //消费速率
    double getMsgRateOut();
    //上一次统计到现在发送给消费者的消息总数
    long getMsgOutCounter();
    //发送给消费者的消息总数
    long getTotalMsgOutCounter();
    //最近一次ack时间
    long getLastAckedTimestamp();
    //最近一次消费时间
    long getLastMsgOutTimestamp();
    //最近一次ack的位移
    long getLastAckedPosition();
    //最近一次发送给消费者消息位移
    long getLastMsgOutPosition();
    //订阅状态
    int getSubscriptionState();

    List<? extends ConsumerStats> getConsumers();
}
