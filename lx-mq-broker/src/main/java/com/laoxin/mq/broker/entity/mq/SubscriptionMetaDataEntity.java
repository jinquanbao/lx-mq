package com.laoxin.mq.broker.entity.mq;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SubscriptionMetaDataEntity {

    private String dependencyOnSubscription;

    private String filterExpression;

    private long ackTimeOut;

    private boolean enablePush;

    private Set<SubscriptionConsumer> consumers;

    private Map<String, String> subscriptionProperties;
}
