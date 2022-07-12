package com.laoxin.mq.broker.trace;

import com.laoxin.mq.broker.Application;
import com.laoxin.mq.broker.service.BrokerService;
import com.laoxin.mq.client.api.MessageId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class TraceLogFlush2RdbTest {

    @Autowired
    private BrokerService brokerService;

    @Test
    public void flushTest(){
        final TraceLogFlush2Rdb flush2Rdb = new TraceLogFlush2Rdb(brokerService.springContext().traceLogMapper());

        final MessageId messageId = MessageId.from("test", 1, 100);

        List<TraceLogInfo> traces = new ArrayList<>();
        TraceLogInfo trace0 = new TraceLogInfo();
        TraceLogInfo trace1 = new TraceLogInfo();
        TraceLogInfo trace2 = new TraceLogInfo();
        TraceLogInfo trace3 = new TraceLogInfo();
        traces.add(trace0);
        traces.add(trace1);
        traces.add(trace2);
        traces.add(trace3);

        trace0.setMessageId(messageId);
        trace0.setStep(TraceStepEnum.msg_in);
        trace0.setProducerTraceLogInfo(
                ProducerTraceLogInfo.builder()
                        .msgCreateTime(new Date().getTime()-1000)
                        .producerAddress("127.0.0.1")
                        .producerName("producer")
                        .build()
        );

        trace1.setMessageId(messageId);
        trace1.setStep(TraceStepEnum.msg_stored);
        trace1.setProducerTraceLogInfo(
                ProducerTraceLogInfo.builder()
                        .msgStoredTime(new Date().getTime())
                        .producerAddress("127.0.0.1")
                        .producerName("producer")
                        .build()
        );

        trace2.setMessageId(messageId);
        trace2.setStep(TraceStepEnum.msg_outed);
        trace2.setConsumerTraceLogInfo(ConsumerTraceLogInfo.builder()
                .subscriptionName("subscriptionA")
                .address("127.0.0.1")
                .consumerName("consumerA")
                .subscriptionType("S")
                .msgOutTimestamp(new Date().getTime()-1000)
                .build());

        trace3.setMessageId(messageId);
        trace3.setStep(TraceStepEnum.msg_acked);
        trace3.setConsumerTraceLogInfo(ConsumerTraceLogInfo.builder()
                .subscriptionName("subscriptionA")
                .address("127.0.0.1")
                .consumerName("consumerA")
                .subscriptionType("S")
                .ackTimestamp(new Date().getTime())
                .build());

        //add
        flush2Rdb.flush(traces);

        //update
        trace2.getConsumerTraceLogInfo().setSubscriptionName("subscriptionB");
        trace2.getConsumerTraceLogInfo().setConsumerName("consumerB");
        trace3.getConsumerTraceLogInfo().setSubscriptionName("subscriptionB");
        trace3.getConsumerTraceLogInfo().setConsumerName("consumerB");
        flush2Rdb.flush(traces);

    }
}
