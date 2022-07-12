package com.laoxin.mq.broker.trace;

import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageId;

import java.util.List;
import java.util.stream.Collectors;

public class TraceLogConvert {

    public static List<TraceLogInfo> convert(List<Message> list,ProducerTraceLogInfo producerTraceLogInfo,ConsumerTraceLogInfo traceLogInfo,TraceStepEnum step){
        if(list == null || list.isEmpty()){
            return null;
        }
        return list.stream().map(x->
                convert(x.getMessageId(),producerTraceLogInfo,traceLogInfo,step))
                .collect(Collectors.toList());
    }

    public static TraceLogInfo convert(MessageId messageId, ProducerTraceLogInfo producerTraceLogInfo, ConsumerTraceLogInfo traceLogInfo, TraceStepEnum step){
        if(messageId == null){
            return null;
        }
        return TraceLogInfo.builder()
                .messageId(messageId)
                .step(step)
                .producerTraceLogInfo(producerTraceLogInfo)
                .consumerTraceLogInfo(traceLogInfo)
                .build();
    }

    public static List<TraceLogInfo> convert(List<MessageId> messageIds, ConsumerTraceLogInfo traceLogInfo, TraceStepEnum step){
        if(messageIds == null || messageIds.isEmpty()){
            return null;
        }
        return messageIds.stream().map(x->
                convert(x,null,traceLogInfo,step))
                .collect(Collectors.toList());
    }
}
