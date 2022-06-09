package com.laoxin.mq.broker.service;


import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode
public class TopicKey {

    @ApiModelProperty(value = "租户id")
    private long tenantId;

    @ApiModelProperty(value = "topic名称")
    private String topicName;
}
