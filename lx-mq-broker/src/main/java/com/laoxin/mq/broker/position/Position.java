package com.laoxin.mq.broker.position;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class Position implements Comparable<Position>{

    private PositionKey positionKey;

    private long entryId;

    @Override
    public int compareTo(Position o) {
        return ((Long)this.entryId).compareTo(o.getEntryId());
    }
}
