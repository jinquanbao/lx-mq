package com.laoxin.mq.broker.entity.mq.trace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProducerLogEntity {

    private String producerName;

    private String producerAddress;

    private Long msgCreateTime;
}
