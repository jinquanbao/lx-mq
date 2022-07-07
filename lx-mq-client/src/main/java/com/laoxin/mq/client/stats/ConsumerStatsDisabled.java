package com.laoxin.mq.client.stats;

import java.util.concurrent.atomic.LongAdder;

public class ConsumerStatsDisabled implements ConsumerStatsRecorder{

    static final ConsumerStatsRecorder INSTANCE = new ConsumerStatsDisabled();
    private LongAdder longAdder = new LongAdder();
    @Override
    public void incrementIntervalMsgReceived(long num) {

    }

    @Override
    public void incrementIntervalMsgReceivedFailed(long num) {

    }

    @Override
    public void intervalCalculate() {

    }

    @Override
    public String getSubscriptionName() {
        return null;
    }

    @Override
    public long getConnectedTimestamp() {
        return 0;
    }

    @Override
    public long getDisconnectedTimestamp() {
        return 0;
    }

    @Override
    public long getMsgReceived() {
        return 0;
    }

    @Override
    public long getMsgReceivedFailed() {
        return 0;
    }

    @Override
    public LongAdder getTotalMsgReceived() {
        return longAdder;
    }

    @Override
    public LongAdder getTotalMsgReceivedFailed() {
        return longAdder;
    }

    @Override
    public long getLastAckedTimestamp() {
        return 0;
    }

    @Override
    public long getLastMsgReceivedTimestamp() {
        return 0;
    }

    @Override
    public long getLastAckedPosition() {
        return 0;
    }

    @Override
    public long getLastMsgReceivedPosition() {
        return 0;
    }

    @Override
    public double getMsgRate() {
        return 0;
    }

    @Override
    public int getConnectionStatus() {
        return 0;
    }

    @Override
    public Throwable getException() {
        return null;
    }

    @Override
    public long getExceptionTimestamp() {
        return 0;
    }
}
