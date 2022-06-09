package com.laoxin.mq.broker.position;

import com.laoxin.mq.broker.enums.StoreType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MemoryPositionOffsetStore implements PositionOffsetStore {

    private Map<PositionKey, Position> cache = new ConcurrentHashMap<>();

    @Override
    public boolean storageType(String storageType) {
        return StoreType.memory.name().equalsIgnoreCase(storageType);
    }

    @Override
    public Optional<Position> getPosition(PositionKey positionKey) {
        return Optional.ofNullable(cache.get(positionKey));
    }

    @Override
    public boolean persist(Position position) {
        if(position == null || position.getPositionKey() == null){
            return false;
        }
        Position old = cache.get(position.getPositionKey());
        if(old != null && old.compareTo(position) >0 ){
            log.warn("current position is Less than old persist position,newPosition={},oldPosition={}",position,old);
            return false;
        }
        cache.put(position.getPositionKey(),position);
        return true;
    }
}
