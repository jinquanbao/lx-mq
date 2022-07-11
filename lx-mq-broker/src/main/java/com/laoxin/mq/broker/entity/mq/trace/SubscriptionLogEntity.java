package com.laoxin.mq.broker.entity.mq.trace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubscriptionLogEntity {

    //订阅名称
    private String subscriptionName;

    //订阅模式
    private String subscriptionType;

    //消费者信息
    private Set<ConsumerLogEntity> consumers;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionLogEntity that = (SubscriptionLogEntity) o;
        return Objects.equals(subscriptionName, that.subscriptionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionName);
    }

    public SubscriptionLogEntity merge(SubscriptionLogEntity old,SubscriptionLogEntity update){
        SubscriptionLogEntity ret = old;
        ret.consumers = ret.consumers == null?new HashSet<>():ret.consumers;
        if(update.consumers != null && !update.consumers.isEmpty()){
            ret.consumers.addAll(update.consumers);
        }
        return ret;
    }
}
