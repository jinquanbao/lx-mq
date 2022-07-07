package com.laoxin.mq.client.stats;

import java.util.concurrent.atomic.LongAdder;

public class ProducerStatsDisabled implements ProducerStatsRecorder{

    static final ProducerStatsRecorder INSTANCE = new ProducerStatsDisabled();

    private LongAdder longAdder = new LongAdder();

    @Override
    public void incrementIntervalMsgOut(long num) {

    }

    @Override
    public void incrementIntervalMsgOutFailed(long num) {

    }

    @Override
    public void intervalCalculate() {

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
    public String getProducerName() {
        return null;
    }

    @Override
    public double getMsgRateOut() {
        return 0;
    }

    @Override
    public long getMsgOutCounter() {
        return 0;
    }

    @Override
    public long getMsgOutFailedCounter() {
        return 0;
    }

    @Override
    public LongAdder getTotalMsgOutCounter() {
        return longAdder;
    }

    @Override
    public LongAdder getTotalMsgOutFailedCounter() {
        return longAdder;
    }

    @Override
    public long getLastMsgOutTimestamp() {
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
