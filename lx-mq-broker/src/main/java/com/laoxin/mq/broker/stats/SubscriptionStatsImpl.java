package com.laoxin.mq.broker.stats;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubscriptionStatsImpl implements SubscriptionStats{

    //订阅名称
    private String subscriptionName;
    //租户ID
    private long tenantId;
    //订阅连接时间
    private long connectedTimestamp;
    //消费速率 msg/s
    private double msgRateOut;
    //上一次统计到现在发送给消费者的消息总数
    public long msgOutCounter;
    //发送给消费者的消息总数
    public long totalMsgOutCounter;
    //最近一次ack时间
    public long lastAckedTimestamp;
    //最近一次消费时间
    public long lastMsgOutTimestamp;
    //最近一次ack的位移
    private long lastAckedPosition;
    //最近一次发送给消费者消息位移
    private long lastMsgOutPosition;
    //订阅状态
    private int subscriptionState;

    private List<ConsumerStatsRecorder> consumers;

    public SubscriptionStatsImpl(){
        consumers = new ArrayList<>();
    }

    public void add(ConsumerStatsRecorder consumerStats){
        consumers.add(consumerStats);
        msgRateOut +=consumerStats.getMsgRateOut();
        lastAckedTimestamp = (Math.max(lastAckedTimestamp,consumerStats.getLastAckedTimestamp()));
        lastAckedPosition = (Math.max(lastAckedPosition,consumerStats.getLastAckedPosition()));
        lastMsgOutPosition = (Math.max(lastMsgOutPosition,consumerStats.getLastMsgOutPosition()));
        lastMsgOutTimestamp = (Math.max(lastMsgOutTimestamp,consumerStats.getLastMsgOutTimestamp()));
        msgOutCounter +=consumerStats.getMsgOutCounter();
        totalMsgOutCounter +=consumerStats.getTotalMsgOutCounter().longValue();
        connectedTimestamp = (Math.max(connectedTimestamp, consumerStats.getConnectedTimestamp()));
    }

    public void reset(){
        msgRateOut = 0;
        msgOutCounter = 0;
        totalMsgOutCounter = 0;
        lastAckedTimestamp = 0;
        lastMsgOutTimestamp = 0;
        lastAckedPosition = 0;
        lastMsgOutPosition = 0;
        subscriptionState = 0;
        consumers = new ArrayList<>();
    }
}
