package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.entity.mq.BinLogStoreEntity;
import com.laoxin.mq.broker.enums.StoreType;
import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.broker.position.PositionKey;
import com.laoxin.mq.client.api.BinlogSubscriptionProperties;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageBuilder;
import com.laoxin.mq.client.api.MessageId;
import com.laoxin.mq.client.impl.MessageIdImpl;
import com.laoxin.mq.client.impl.MessageImpl;
import com.laoxin.mq.client.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class RdbBinlogMessageStore implements MessageStore {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final int MAX_READE_SIZE = 100;

    RdbBinlogMessageStore(DataSource dataSource){
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean storageType(String storageType) {
        return StoreType.rdb.name().equalsIgnoreCase(storageType);
    }

    @Override
    public CompletableFuture<List<Message>> readMessage(ReadMessageRequest request) {

        Position position = request.getPosition();
        Long maxEntryId = request.getMaxEntryId();
        int readSize = request.getReadSize()>MAX_READE_SIZE?MAX_READE_SIZE:request.getReadSize();
        final Map<String, String> subscriptionProperties = request.getSubscriptionProperties();

        Set<String> tableNames = null;
        Set<String> eventTypes = null;

        if(subscriptionProperties != null){
            BinlogSubscriptionProperties properties = BinlogSubscriptionProperties.from(subscriptionProperties);
            tableNames = properties.getTableNames();
            eventTypes = properties.getEventTypes();
        }

        final PositionKey dto = position.getPositionKey();

        StringBuilder sql = new StringBuilder("select * from ")
                .append(dto.getTopic()+"_"+dto.getTenantId())
                .append(" where 1=1 ")
                ;

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("offsetId",position.getEntryId());
        paramMap.put("maxOffsetId",maxEntryId);
        paramMap.put("tenant_id",dto.getTenantId());

        sql.append(" and id>:offsetId");
        if(maxEntryId != null){
            sql.append(" and id<=:maxOffsetId");
        }
        sql.append(" and tenant_id=:tenant_id");

        if(tableNames != null && !tableNames.isEmpty()){
            paramMap.put("table_names",tableNames);
            sql.append(" and table_name in (:table_names)");
        }
        if(eventTypes != null && !eventTypes.isEmpty()){
            paramMap.put("event_types",eventTypes);
            sql.append(" and event_type in (:event_types)");
        }

        sql.append(" order by id asc limit :limit");
        paramMap.put("limit",readSize);

        if(log.isTraceEnabled()){
            log.trace(sql.toString());
        }

        List<BinLogStoreEntity> result = jdbcTemplate.query(sql.toString(), paramMap, new BeanPropertyRowMapper(BinLogStoreEntity.class));

        final List<Message> list = result
                .stream()
                .map(entity -> MessageBuilder.create()
                        .setContent(entity)
                        .setMessageId(MessageId.from(dto.getTopic(), dto.getTenantId(), entity.getId()))
                        .build()
                ).collect(Collectors.toList());


        return CompletableFuture.completedFuture(list);
    }

    @Override
    public MessageIdImpl writeMessage(TopicKey topicKey,String msgStr) {

        MessageImpl<BinLogStoreEntity> message = JSONUtil.fromJson(msgStr, new ParameterizedTypeReference<MessageImpl<BinLogStoreEntity>>() {
        });

        return (MessageIdImpl)MessageId.from(topicKey.getTopicName(),topicKey.getTenantId(), new
                Random().nextInt(1000));
    }


}
