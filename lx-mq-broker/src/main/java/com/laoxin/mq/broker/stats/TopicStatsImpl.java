package com.laoxin.mq.broker.stats;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TopicStatsImpl implements TopicStats{
    //订阅名称
    private String topicName;
    //租户ID
    private long tenantId;

    //发送速率 msg/s
    private double msgRateIn;
    //上一次统计到现在发送到主题的消息总数
    private long msgInCounter;
    //发送到主题的消息总数
    private long totalMsgInCounter;

    //消费速率 msg/s
    private double msgRateOut;
    //上一次统计到现在主题消费的消息总数
    public long msgOutCounter;
    //主题消费的消息总数
    private long totalMsgOutCounter;

    //生产者消息统计
    private final List<ProducerStatsImpl> producers;

    //主题下的订阅统计
    private final List<SubscriptionStatsImpl> subscriptions;

    public TopicStatsImpl(){
        this.producers = new ArrayList();
        this.subscriptions = new ArrayList();
    }

    public void add(ProducerStatsImpl stats){
        this.msgRateIn += stats.getMsgRateIn();
        this.msgInCounter += stats.getMsgInCounter();
        this.totalMsgInCounter += stats.getTotalMsgInCounter().longValue();
        this.producers.add(stats);
    }

    public void add(SubscriptionStatsImpl stats){
        this.msgRateOut += stats.getMsgRateOut();
        this.msgOutCounter += stats.getMsgOutCounter();
        this.totalMsgOutCounter += stats.getTotalMsgOutCounter();
        this.subscriptions.add(stats);
    }

}
