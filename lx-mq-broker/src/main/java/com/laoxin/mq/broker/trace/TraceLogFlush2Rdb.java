package com.laoxin.mq.broker.trace;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.laoxin.mq.broker.entity.mq.TraceLogEntity;
import com.laoxin.mq.broker.entity.mq.trace.ProducerLogEntity;
import com.laoxin.mq.broker.entity.mq.trace.SubscriptionLogEntity;
import com.laoxin.mq.broker.entity.mq.trace.TraceLogTempEntity;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.mapper.mq.TraceLogMapper;
import com.laoxin.mq.client.util.JSONUtil;

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
                }, (k1, k2) -> k1.merge(k1,k2)))
                .values()
                .stream()
                .collect(Collectors.toList());

        entities.forEach(entity->{
            final TraceLogEntity one = getOne(entity.getTenantId(), entity.getTopicName(), entity.getMessageId());
            TraceLogTempEntity old = convert(one);
            if(old != null){
                TraceLogEntity update = convert(entity.merge(old,entity));
                update.setId(one.getId());
                mapper.updateById(update);
            }else {
                mapper.insert(convert(entity));
            }
        });

        return CompletableFuture.completedFuture(null);
    }

    private TraceLogEntity convert(TraceLogTempEntity entity){
        if(entity == null){
            return null;
        }
        return TraceLogEntity.builder()
                .id(entity.getId())
                .messageId(entity.getMessageId())
                .tenantId(entity.getTenantId())
                .topicName(entity.getTopicName())
                .producerLog(entity.getProducerLog() == null?"{}":JSONUtil.toJson(entity.getProducerLog()))
                .subscriptionLog(entity.getSubscriptionLog() == null?"[]":JSONUtil.toJson(entity.getSubscriptionLog()))
                .build();
    }

    private TraceLogTempEntity convert(TraceLogEntity entity){
        if(entity == null){
            return null;
        }
        return TraceLogTempEntity.builder()
                .id(entity.getId())
                .messageId(entity.getMessageId())
                .tenantId(entity.getTenantId())
                .topicName(entity.getTopicName())
                .producerLog(JSONUtil.fromJson(entity.getProducerLog(), ProducerLogEntity.class))
                .subscriptionLog(JSONUtil.fromJsonArray(entity.getSubscriptionLog(), SubscriptionLogEntity.class))
                .build();
    }

    private TraceLogEntity getOne(long tenantId,String topic, long entryId){
        final LambdaQueryWrapper<TraceLogEntity> wrapper = Wrappers.lambdaQuery(TraceLogEntity.class);
        wrapper.eq(TraceLogEntity::getTenantId,tenantId)
                .eq(TraceLogEntity::getTopicName,topic)
                .eq(TraceLogEntity::getMessageId,entryId);

        final List<TraceLogEntity> list = mapper.selectList(wrapper);
        if(list == null || list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

}
