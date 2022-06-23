package com.laoxin.mq.broker.position;

import java.util.Optional;

public interface PositionOffsetStore {

    boolean storageType(String storageType);

    default void start(){};

    default void close(){};

    Optional<Position> getPosition(PositionKey positionKey);

    //持久化的位移必须比上一次的位移大
    boolean persist(Position position);

    //持久化的位移直接更新
    boolean seek(Position position);
}
