package com.laoxin.mq.broker.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode
@ToString
public class SubscriptionKey {

    @ApiModelProperty(value = "租户id")
    private Long tenantId;


    @ApiModelProperty(value = "订阅名称")
    private String subscriptionName;


    @ApiModelProperty(value = "topic名称")
    private String topicName;

}
