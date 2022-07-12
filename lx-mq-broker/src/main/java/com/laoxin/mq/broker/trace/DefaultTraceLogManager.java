package com.laoxin.mq.broker.trace;

import com.laoxin.mq.broker.mapper.mq.TraceLogMapper;

public class DefaultTraceLogManager implements TraceLogManager{

    private final TraceLogContext traceLogContext;
    private final TraceLogFlush traceLogFlush;

    public DefaultTraceLogManager(TraceLogMapper traceLogMapper,boolean enableTrace){
        this.traceLogFlush = new TraceLogFlush2Rdb(traceLogMapper);
        if(enableTrace){
            traceLogContext = new TraceLogContextImpl(traceLogFlush);
        }else {
            traceLogContext = new TraceLogContextDisabled();
        }
    }

    @Override
    public TraceLogContext traceLogContext() {
        return traceLogContext;
    }

    @Override
    public TraceLogFlush traceLogFlush() {
        return traceLogFlush;
    }
}
