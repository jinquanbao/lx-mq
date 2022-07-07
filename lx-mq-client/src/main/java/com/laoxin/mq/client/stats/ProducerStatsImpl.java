package com.laoxin.mq.client.stats;

import lombok.Data;

import java.util.concurrent.atomic.LongAdder;

@Data
public class ProducerStatsImpl implements ProducerStatsRecorder{
    //生产者名称
    private String producerName;
    //订阅连接时间
    private long connectedTimestamp;
    //订阅断开时间
    private long disconnectedTimestamp;

    //时间周期内接收的消息数
    private final LongAdder intervalMsgOutCounter;

    private long lastIntervalMsgOutCounter;

    //时间周期内接收失败的消息数
    private final LongAdder intervalMsgOutFailedCounter;

    private long lastIntervalMsgOutFailedCounter;

    //总共接收的消息数
    private final LongAdder totalMsgOutCounter;
    //总共接收失败的消息数
    private final LongAdder totalMsgOutFailedCounter;

    //最近一次发送消息的时间
    private long lastMsgOutTimestamp;

    //消息消费速率
    private volatile double msgRateOut;
    //连接状态
    private volatile int connectionStatus;
    //消费异常信息
    private volatile Throwable exception;
    //发生异常的时间
    private long exceptionTimestamp;

    private long oldTime;

    public ProducerStatsImpl(){
        this.intervalMsgOutFailedCounter = new LongAdder();
        this.intervalMsgOutCounter = new LongAdder();
        this.totalMsgOutCounter = new LongAdder();
        this.totalMsgOutFailedCounter = new LongAdder();
        this.oldTime = System.nanoTime();
    }

    public ProducerStatsImpl(String producerName){
        this();
        this.producerName = producerName;
    }

    public void incrementIntervalMsgOut(long num) {
        this.intervalMsgOutCounter.add(num);
    }

    public long getMsgOutCounter() {
        return lastIntervalMsgOutCounter;
    }

    public void incrementIntervalMsgOutFailed(long num) {
        this.intervalMsgOutFailedCounter.add(num);
    }

    public long getMsgOutFailedCounter() {
        return lastIntervalMsgOutFailedCounter;
    }

    public void intervalCalculate() {
        this.lastIntervalMsgOutCounter = this.intervalMsgOutCounter.sumThenReset();
        this.lastIntervalMsgOutFailedCounter = intervalMsgOutFailedCounter.sumThenReset();
        final long now = System.nanoTime();
        double interval = (now - oldTime) / 1e9;
        this.msgRateOut= this.lastIntervalMsgOutCounter/interval;
        this.totalMsgOutCounter.add(lastIntervalMsgOutCounter);
        this.totalMsgOutFailedCounter.add(lastIntervalMsgOutFailedCounter);
        this.oldTime = now;
    }
}
