package com.laoxin.mq.broker.stats;

public interface ConsumerStats {

    String getConsumerName();
    long getTenantId();
    String getAddress();

    double getMsgRateOut();

    long getMsgOutCounter();

    long getConnectedTimestamp();

    //最近一次ack时间
    long getLastAckedTimestamp();
    //最近一次消费时间
    long getLastMsgOutTimestamp();
    //最近一次ack的位移
    long getLastAckedPosition();
    //最近一次发送给消费者消息位移
    long getLastMsgOutPosition();

    void calculateRate();
}
