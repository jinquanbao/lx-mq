
package com.laoxin.mq.client.enums;

import java.util.Arrays;

/**
 * Types of subscription supported
 */
public enum SubscriptionType {
    //直连
    Direct,
    //共享
    Shared,

    ;
    public static SubscriptionType getEnum(String subscriptionType){
        return Arrays.stream(SubscriptionType.values())
                .filter(x->x.name().equals(subscriptionType))
                .findAny()
                .orElse(null);
    }
}