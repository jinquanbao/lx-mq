package com.laoxin.mq.broker.service;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class ProducerKey {
    private long tenantId;
    private long producerId;
}
