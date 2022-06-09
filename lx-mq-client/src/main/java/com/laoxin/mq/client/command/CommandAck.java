package com.laoxin.mq.client.command;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class CommandAck {

    private long consumerId;
    private long tenantId;
    private String topic;
    private String subscription;
    private long entryId;

}
