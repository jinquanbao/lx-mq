package com.laoxin.mq.broker.stats;


import java.util.concurrent.atomic.LongAdder;

public class ConsumerStatsDisabled implements ConsumerStatsRecorder{
    static final ConsumerStatsRecorder INSTANCE = new ConsumerStatsDisabled();

    private LongAdder longAdder = new LongAdder();

    @Override
    public void incrementMsgOutCounter(long num) {

    }

    @Override
    public void setAddress(String address) {

    }

    @Override
    public void setConnectedTimestamp(long connectedTimestamp) {

    }

    @Override
    public void setMsgRateOut(double msgRateOut) {

    }

    @Override
    public void setMsgOutCounter(long msgOutCounter) {

    }

    @Override
    public void setLastAckedTimestamp(long lastAckedTimestamp) {

    }

    @Override
    public void setLastMsgOutTimestamp(long lastMsgOutTimestamp) {

    }

    @Override
    public void setLastAckedPosition(long lastAckedPosition) {

    }

    @Override
    public void setLastMsgOutPosition(long lastMsgOutPosition) {

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
    public String getConsumerName() {
        return null;
    }

    @Override
    public long getTenantId() {
        return 0;
    }

    @Override
    public String getAddress() {
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
    public LongAdder getTotalMsgOutCounter() {
        return longAdder;
    }

    @Override
    public long getConnectedTimestamp() {
        return 0;
    }

    @Override
    public long getLastAckedTimestamp() {
        return 0;
    }

    @Override
    public long getLastMsgOutTimestamp() {
        return 0;
    }

    @Override
    public long getLastAckedPosition() {
        return 0;
    }

    @Override
    public long getLastMsgOutPosition() {
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
