package com.laoxin.mq.client.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class Sequence {

    private long requestId;

    private long channelId;


    public static Sequence newSequence(long requestId,long channelId) {
        return new Sequence(requestId,channelId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        return requestId == sequence.requestId &&
                channelId == sequence.channelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, channelId);
    }
}
