package com.laoxin.mq.broker.trace;

import com.laoxin.mq.client.api.MessageId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TraceLogInfo {

    private MessageId messageId;

    private ProducerTraceLogInfo producerTraceLogInfo;

    private ConsumerTraceLogInfo consumerTraceLogInfo;

    private TraceStepEnum step;

}
