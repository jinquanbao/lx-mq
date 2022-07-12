package com.laoxin.mq.broker.trace;

import java.util.List;

public interface TraceLogContext {

    void start();

    boolean log(TraceLogInfo info);

    void log(List<TraceLogInfo> info);

    void close();

    default boolean logEnabled(){
        return true;
    };
}
