package com.laoxin.mq.broker.service;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class ConsumerKey {
    private long tenantId;
    private long consumerId;
}
