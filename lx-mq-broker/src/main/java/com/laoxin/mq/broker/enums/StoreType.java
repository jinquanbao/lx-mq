package com.laoxin.mq.broker.enums;

import java.util.Arrays;

public enum StoreType {

    memory,
    file,
    rdb,
    ;
    public static StoreType getEnum(String messageStoreType){
        return Arrays.stream(StoreType.values())
                .filter(x->x.name().equals(messageStoreType))
                .findAny()
                .orElse(null);
    }
}
