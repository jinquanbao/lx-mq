package com.laoxin.mq.broker.trace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProducerTraceLogInfo {

    private String producerName;

    private String producerAddress;

    private Long msgCreateTime;

    private Long msgStoredTime;

}
