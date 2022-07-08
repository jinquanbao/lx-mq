package com.laoxin.mq.broker.trace;

public interface TraceLogContext {

    void start();

    boolean log(TraceLogInfo info);

    void close();

    default boolean logDisabled(){
        return true;
    };
}
