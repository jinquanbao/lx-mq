package com.laoxin.mq.broker.trace;

public class TraceLogContextDisabled implements TraceLogContext{

    @Override
    public void start() {

    }

    @Override
    public boolean log(TraceLogInfo info) {
        return false;
    }

    @Override
    public void close() {

    }

}
