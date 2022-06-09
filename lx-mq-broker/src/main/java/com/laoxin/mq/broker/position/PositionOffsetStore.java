package com.laoxin.mq.broker.position;

import java.util.Optional;

public interface PositionOffsetStore {

    boolean storageType(String storageType);

    default void start(){};

    default void close(){};

    Optional<Position> getPosition(PositionKey positionKey);

    boolean persist(Position position);
}
