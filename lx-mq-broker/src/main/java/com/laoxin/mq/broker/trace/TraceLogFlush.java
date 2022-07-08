package com.laoxin.mq.broker.trace;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TraceLogFlush {

    void flush(List<TraceLogInfo> traces);

    CompletableFuture<Void> flushAsync(List<TraceLogInfo> traces);
}
