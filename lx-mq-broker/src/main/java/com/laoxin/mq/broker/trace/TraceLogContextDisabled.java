package com.laoxin.mq.broker.trace;

import java.util.List;

public class TraceLogContextDisabled implements TraceLogContext{

    @Override
    public void start() {

    }

    @Override
    public boolean log(TraceLogInfo info) {
        return false;
    }

    @Override
    public void log(List<TraceLogInfo> info) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean logEnabled() {
        return false;
    }
}
