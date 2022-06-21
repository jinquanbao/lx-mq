package com.laoxin.mq.broker.position;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString
public class PositionKey {

    private long tenantId;

    private String topic;

    private String subscription;

    public String key(){
        return tenantId+"@"+topic+"@"+subscription;
    }
}
