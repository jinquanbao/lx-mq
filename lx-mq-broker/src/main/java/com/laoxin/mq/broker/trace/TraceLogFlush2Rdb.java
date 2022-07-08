package com.laoxin.mq.broker.trace;

import com.laoxin.mq.broker.entity.mq.trace.TraceLogTempEntity;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.mapper.mq.TraceLogMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TraceLogFlush2Rdb implements TraceLogFlush{

    private final TraceLogMapper mapper;

    public TraceLogFlush2Rdb(TraceLogMapper mapper){
        this.mapper = mapper;
    }

    @Override
    public void flush(List<TraceLogInfo> traces) {
        try {
            flushAsync(traces).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MqServerException(e);
        } catch (ExecutionException e) {
            throw new MqServerException(e);
        }
    }

    @Override
    public CompletableFuture<Void> flushAsync(List<TraceLogInfo> traces) {
        if(traces == null || traces.isEmpty()){
            return CompletableFuture.completedFuture(null);
        }
        List<TraceLogTempEntity> entities = traces.stream().filter(x -> x != null)
                .collect(Collectors.toMap(x -> x.getMessageId(), x -> {
                    final TraceLogTempEntity entity = TraceLogTempEntity.builder()
                            .messageId(x.getMessageId().getEntryId())
                            .build();
                    return entity;
                }, (k1, k2) -> k1.merge(k2)))
                .values()
                .stream()
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(null);
    }
}
