package com.laoxin.mq.broker.position;

import com.laoxin.mq.broker.entity.mq.PositionOffsetEntity;
import com.laoxin.mq.broker.enums.StoreType;
import com.laoxin.mq.broker.exception.MqServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public class RdbPositionOffsetStore implements PositionOffsetStore {

    private final JdbcTemplate template;

    String INSERT_SQL = new StringBuilder()
            .append("insert into position_offset(id,last_offset_id,current_offset_id,update_time)")
            .append(" VALUES(?,?,?,?)").toString();

    String UPDATE_SQL = new StringBuilder()
            .append("update position_offset set last_offset_id=?,current_offset_id=?,update_time=? where id=? and current_offset_id=? and current_offset_id<?").toString();

    String LIST_ONE_SQL = "select last_offset_id as lastOffsetId,current_offset_id as currentOffsetId from position_offset where id = ?";

    public RdbPositionOffsetStore(DataSource dataSource){
        this.template = new JdbcTemplate(dataSource);
    }

    PositionOffsetEntity getById(String id){
        final List<PositionOffsetEntity> list = template.query(LIST_ONE_SQL, new BeanPropertyRowMapper(PositionOffsetEntity.class), id);

        return (list == null||list.isEmpty())?null:list.get(0);
    }

    @Override
    public boolean storageType(String storageType) {
        return StoreType.rdb.name().equalsIgnoreCase(storageType);
    }

    @Override
    public Optional<Position> getPosition(PositionKey positionKey) {
        final PositionOffsetEntity entity = getById(positionKey.key());

        return Optional.ofNullable(entity == null?null:
                Position.builder()
                        .positionKey(positionKey)
                        .entryId(entity.getCurrentOffsetId())
                        .build());
    }

    @Override
    public boolean persist(Position position) {
        String id = position.getPositionKey().key();
        final PositionOffsetEntity entity = getById(id);
        if(entity == null){
            save(PositionOffsetEntity
                    .builder()
                    .id(id)
                    .lastOffsetId(0)
                    .currentOffsetId(position.getEntryId())
                    .build());

        }else {
            if(entity.getCurrentOffsetId() >= position.getEntryId()){
                log.warn("current position is Less than old persist position,newPosition={},oldPositionId={}",position,entity.getCurrentOffsetId());
                return false;
            }
            int update = template.update(UPDATE_SQL, entity.getCurrentOffsetId(), position.getEntryId(), LocalDateTime.now(), id, entity.getCurrentOffsetId(),position.getEntryId());
            if(update == 0){
                log.warn("Position Offset update result 0 position={}",position);
                return false;
            }
        }
        return true;
    }

    public void save(PositionOffsetEntity entity) {
        if(entity == null || entity.getId() == null || entity.getCurrentOffsetId() <1 ){
            log.error("Position Offset Entity invalid");
            throw new MqServerException("Position Offset Entity invalid");
        }

        template.update(INSERT_SQL,entity.getId(),entity.getLastOffsetId(),entity.getCurrentOffsetId(), LocalDateTime.now());
    }


}
