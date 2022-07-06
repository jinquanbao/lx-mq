package com.laoxin.mq.broker.stats;

import lombok.Data;

import java.util.concurrent.atomic.LongAdder;

@Data
public class ConsumerStatsImpl implements ConsumerStats{

    //消费者名称
    public String consumerName;
    //租户id
    public long tenantId;
    //消费者地址
    public String address;
    //消费者连接时间
    public long connectedTimestamp;

    //消费者消费速率 msg/s
    public double msgRateOut;

    public long msgOutCounter;
    //发送给消费者的消息总数
    public final LongAdder intervalMsgOutCounter;
    public final LongAdder totalMsgOutCounter;
    //最近一次ack时间
    public long lastAckedTimestamp;
    //最近一次消费时间
    public long lastMsgOutTimestamp;
    //最近一次ack的位移
    private long lastAckedPosition;
    //最近一次发送给消费者消息位移
    private long lastMsgOutPosition;

    private final long oldTime;

    public ConsumerStatsImpl() {
        this.intervalMsgOutCounter = new LongAdder();
        this.totalMsgOutCounter = new LongAdder();
        this.oldTime = System.nanoTime();
    }

    public ConsumerStatsImpl(long tenantId, String consumerName){
        this();
        this.tenantId = tenantId;
        this.consumerName = consumerName;
    }

    public void incrementMsgOutCounter(long num){
        this.intervalMsgOutCounter.add(num);
        this.totalMsgOutCounter.add(num);
    }

    @Override
    public void calculateRate() {
        this.msgOutCounter = this.intervalMsgOutCounter.sumThenReset();
        final long now = System.nanoTime();
        double interval = (now - oldTime) / 1e9;
        this.msgRateOut= this.msgOutCounter/interval;
    }
}
