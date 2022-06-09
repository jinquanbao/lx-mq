package com.laoxin.mq.client.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommandProducerSuccess {

    private String producerName;
}
