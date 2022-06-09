package com.laoxin.mq.broker.entity.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SubscriptionMetaDataEntity {

    private String dependencyOnSubscription;

    private String filterExpression;

    private long ackTimeOut;

    private boolean enablePush;

    private Set<SubscriptionConsumer> consumers;

}
