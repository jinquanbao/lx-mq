package com.laoxin.mq.client.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommandCloseConsumer {
    private long consumerId;
    private long tenantId;
    private String topic;
    private String subscription;
}
