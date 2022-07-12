package com.laoxin.mq.broker.trace;

public interface TraceLogManager {

    TraceLogContext traceLogContext();

    TraceLogFlush traceLogFlush();
}
