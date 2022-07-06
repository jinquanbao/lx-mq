package com.laoxin.mq.broker.stats;

import java.util.List;

public interface TopicStats {

    //主题名称
    String getTopicName();
    //租户ID
    long getTenantId();

    //发送速率
    double getMsgRateIn();
    //上一次统计到现在发送到主题的消息总数
    long getMsgInCounter();
    //发送到主题的消息总数
    long getTotalMsgInCounter();

    //消费速率
    double getMsgRateOut();
    //上一次统计到现在主题消费的消息总数
    long getMsgOutCounter();
    //主题消费的消息总数
    long getTotalMsgOutCounter();

    //生产者消息统计
    List<? extends ProducerStats> getProducers();

    //主题下的订阅统计
    List<? extends SubscriptionStats> getSubscriptions();
}
