package com.laoxin.mq.client.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommandUnSubscribe {

    private String topic;

    private String subscribe;

    private long consumerId;

    private long tenantId;


}
