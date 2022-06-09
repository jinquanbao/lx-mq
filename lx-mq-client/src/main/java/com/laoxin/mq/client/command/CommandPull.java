package com.laoxin.mq.client.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CommandPull {

    private long consumerId;
    private long tenantId;
    private String topic;
    private String subscription;
    private long entryId;
    private int size;

}
