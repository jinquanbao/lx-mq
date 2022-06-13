package com.laoxin.mq.client.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommandSubscribe {

    private String topic;

    private String topicType;

    private String subscription;

    private String dependencyOnSubscription;

    private String subscribeType;

    @Deprecated
    private String filterExpression;

    private long consumerId;

    private String consumerName;

    private long tenantId;

    private long ackTimeOut;

    private boolean enablePush;

    Map<String, String> subscriptionProperties;


}
