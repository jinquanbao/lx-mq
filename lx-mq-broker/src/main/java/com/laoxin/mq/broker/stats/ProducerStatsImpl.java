package com.laoxin.mq.broker.stats;

import lombok.Data;

import java.util.concurrent.atomic.LongAdder;

@Data
public class ProducerStatsImpl implements ProducerStatsRecorder{

    private String producerName;

    private long tenantId;

    //注册时间
    private long registerTimestamp;

    //发送速率 msg/s
    private double msgRateIn;
    //上一次统计到现在发送到主题的消息总数
    private long msgInCounter;
    private final LongAdder intervalMsgInCounter;
    //发送到主题的消息总数
    private final LongAdder totalMsgInCounter;
    //最近一次生产消息时间
    private long lastMsgInTimestamp;
    //消费异常信息
    private volatile Throwable exception;
    //发生异常的时间
    private long exceptionTimestamp;

    private long oldTime;

    public ProducerStatsImpl() {
        this.oldTime = System.nanoTime();
        this.totalMsgInCounter = new LongAdder();
        this.intervalMsgInCounter = new LongAdder();
    }

    public ProducerStatsImpl(String producerName, long tenantId,long registerTimestamp) {
        this();
        this.producerName = producerName;
        this.tenantId = tenantId;
        this.registerTimestamp = registerTimestamp;
    }

    public void incrementMsgInCounter(long num){
        this.intervalMsgInCounter.add(num);
        this.totalMsgInCounter.add(num);
    }

    @Override
    public void intervalCalculate() {
        calculateRate();
    }

    public void calculateRate() {
        this.msgInCounter = this.intervalMsgInCounter.sumThenReset();
        final long now = System.nanoTime();
        double interval = (now - oldTime) / 1e9;
        this.msgRateIn= this.msgInCounter/interval;
        this.oldTime = now;
    }
}
