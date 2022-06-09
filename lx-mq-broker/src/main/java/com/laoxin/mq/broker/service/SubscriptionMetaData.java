package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SubscriptionMetaData {

    private String subscriptionName;

    private String dependencyOnSubscription;

    private String subscriptionType;

    private String filterExpression;

    private long ackTimeOut;

    private boolean enablePush;

    private SubscriptionConsumer consumer;
}
