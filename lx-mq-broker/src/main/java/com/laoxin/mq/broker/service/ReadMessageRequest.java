package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.position.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ReadMessageRequest {

    private Position position;
    private Long maxEntryId;
    private int readSize;
    private Map<String,String> subscriptionProperties;

}
