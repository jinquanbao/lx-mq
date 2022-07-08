package com.laoxin.mq.broker.trace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConsumerTraceLogInfo {

    //订阅名称
    private String subscriptionName;

    //订阅模式
    private String subscriptionType;

    //消费者名称
    private String consumerName;

    //消费者地址
    private String address;

    //消息最后推送时间
    private Long msgOutTimestamp;

    //消费状态 0-未消费 1-已消费
    private int status;

    //确认消费时间
    private Long ackTimestamp;
}
