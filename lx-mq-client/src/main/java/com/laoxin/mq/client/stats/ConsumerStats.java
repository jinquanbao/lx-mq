package com.laoxin.mq.client.stats;

import java.util.concurrent.atomic.LongAdder;

public interface ConsumerStats {
    //订阅名称
    String getSubscriptionName();
    //连接时间
    long getConnectedTimestamp();
    //断线时间
    long getDisconnectedTimestamp();
    //时间周期内接收的消息数
    long getMsgReceived();
    //时间周期内接收消息失败数
    long getMsgReceivedFailed();
    //总共接收的消息数
    LongAdder getTotalMsgReceived();
    //总共接收失败的消息数
    LongAdder getTotalMsgReceivedFailed();
    //最近一次ack时间
    long getLastAckedTimestamp();
    //最近一次消费时间
    long getLastMsgReceivedTimestamp();
    //最近一次ack的位移
    long getLastAckedPosition();
    //最近一次发送给消费者消息位移
    long getLastMsgReceivedPosition();
    //消息接收速率
    double getMsgRate();
    //连接状态
    int getConnectionStatus();
    //异常信息
    Throwable getException();
    //发生异常的时间
    long getExceptionTimestamp();

}
