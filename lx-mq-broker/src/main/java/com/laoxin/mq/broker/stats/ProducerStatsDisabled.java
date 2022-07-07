package com.laoxin.mq.broker.stats;

import java.util.concurrent.atomic.LongAdder;

public class ProducerStatsDisabled implements ProducerStatsRecorder{

    static final ProducerStatsRecorder INSTANCE = new ProducerStatsDisabled();

    private LongAdder longAdder = new LongAdder();

    @Override
    public void incrementMsgInCounter(long num) {

    }

    @Override
    public void setRegisterTimestamp(long registerTimestamp) {

    }

    @Override
    public void setMsgRateIn(double msgRateIn) {

    }

    @Override
    public void setMsgInCounter(long msgInCounter) {

    }

    @Override
    public void setLastMsgInTimestamp(long lastMsgInTimestamp) {

    }

    @Override
    public void setException(Throwable exception) {

    }

    @Override
    public void setExceptionTimestamp(long exceptionTimestamp) {

    }

    @Override
    public void intervalCalculate() {

    }

    @Override
    public String getProducerName() {
        return null;
    }

    @Override
    public long getTenantId() {
        return 0;
    }

    @Override
    public double getMsgRateIn() {
        return 0;
    }

    @Override
    public long getMsgInCounter() {
        return 0;
    }

    @Override
    public LongAdder getTotalMsgInCounter() {
        return longAdder;
    }

    @Override
    public long getLastMsgInTimestamp() {
        return 0;
    }

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public long getExceptionTimestamp() {
        return 0;
    }
}
