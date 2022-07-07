package com.laoxin.mq.client.stats;

import lombok.Data;

import java.util.concurrent.atomic.LongAdder;

@Data
public class ConsumerStatsImpl implements ConsumerStatsRecorder{
    //订阅名称
    private String subscriptionName;
    //订阅连接时间
    private long connectedTimestamp;
    //订阅断开时间
    private long disconnectedTimestamp;

    //时间周期内接收的消息数
    private final LongAdder intervalMsgReceived;

    private long lastIntervalMsgReceived;

    //时间周期内接收失败的消息数
    private final LongAdder intervalMsgReceivedFailed;

    private long lastIntervalMsgReceivedFailed;

    //总共接收的消息数
    private final LongAdder totalMsgReceived;
    //总共接收失败的消息数
    private final LongAdder totalMsgReceivedFailed;

    //最近一次ack时间
    private long lastAckedTimestamp;
    //最近一次接收到消息的时间
    private long lastMsgReceivedTimestamp;
    //最近一次ack的位移
    private long lastAckedPosition;
    //最近一次接收到的消息位移
    private long lastMsgReceivedPosition;

    //消息消费速率
    private volatile double msgRate;
    //连接状态
    private volatile int connectionStatus;
    //消费异常信息
    private volatile Throwable exception;
    //发生异常的时间
    private long exceptionTimestamp;

    private long oldTime;

    public ConsumerStatsImpl(){
        this.intervalMsgReceived = new LongAdder();
        this.intervalMsgReceivedFailed = new LongAdder();
        this.totalMsgReceived = new LongAdder();
        this.totalMsgReceivedFailed = new LongAdder();
        this.oldTime = System.nanoTime();
    }

    public ConsumerStatsImpl(String subscriptionName){
        this();
        this.subscriptionName = subscriptionName;
    }

    public void incrementIntervalMsgReceived(long num) {
        this.intervalMsgReceived.add(num);
    }

    public long getMsgReceived() {
        return lastIntervalMsgReceived;
    }

    public void incrementIntervalMsgReceivedFailed(long num) {
        this.intervalMsgReceivedFailed.add(num);
    }

    public long getMsgReceivedFailed() {
        return lastIntervalMsgReceivedFailed;
    }

    public void intervalCalculate() {
        this.lastIntervalMsgReceived = this.intervalMsgReceived.sumThenReset();
        this.lastIntervalMsgReceivedFailed = intervalMsgReceivedFailed.sumThenReset();
        final long now = System.nanoTime();
        double interval = (now - oldTime) / 1e9;
        this.msgRate= this.lastIntervalMsgReceived/interval;
        this.totalMsgReceived.add(lastIntervalMsgReceived);
        this.totalMsgReceivedFailed.add(lastIntervalMsgReceivedFailed);
        this.oldTime = now;
    }

}
