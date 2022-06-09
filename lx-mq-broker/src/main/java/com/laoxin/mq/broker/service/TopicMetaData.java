package com.laoxin.mq.broker.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class TopicMetaData {

    @ApiModelProperty(value = "租户id")
    private long tenantId;

    @ApiModelProperty(value = "topic名称")
    private String topicName;

    @ApiModelProperty(value = "topic类型")
    String topicType;
}
