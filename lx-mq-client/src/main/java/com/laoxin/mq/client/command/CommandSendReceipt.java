package com.laoxin.mq.client.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommandSendReceipt {

    private long producerId;

    private long tenantId;

    private long entryId;
}
