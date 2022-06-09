package com.laoxin.mq.broker.entity.mq;

import lombok.*;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SubscriptionConsumer {

    private String address;

    private String consumerName;

    private long subscriptionTime;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionConsumer that = (SubscriptionConsumer) o;
        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
