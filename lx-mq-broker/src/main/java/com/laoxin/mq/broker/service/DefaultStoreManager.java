package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.config.BrokerConfigurationData;
import com.laoxin.mq.broker.exception.MqServerException;
import com.laoxin.mq.broker.position.MemoryPositionOffsetStore;
import com.laoxin.mq.broker.position.PositionOffsetStore;
import com.laoxin.mq.broker.position.RdbPositionOffsetStore;
import com.laoxin.mq.broker.spring.SpringContext;
import com.laoxin.mq.client.enums.TopicType;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class DefaultStoreManager implements StoreManager {

    private final List<MessageStore> defaultMessageStores;
    private final List<MessageStore> binlogMessageStores;
    private final String messageStoreType;
    private final String metaStoreType;
    private final List<MetaStore> metaStores;
    private final DataSource dataSource;
    private final List<PositionOffsetStore> positionOffsetStores;
    private final SpringContext springContext;

    public DefaultStoreManager(DataSource dataSource, SpringContext springContext, BrokerConfigurationData conf){
        this.dataSource = dataSource;
        this.springContext = springContext;
        this.messageStoreType = conf.getMessageStoreType();
        this.metaStoreType = conf.getMetaStoreType();
        this.defaultMessageStores = new ArrayList<>();
        this.binlogMessageStores = new ArrayList<>();
        this.metaStores = new ArrayList<>();
        this.positionOffsetStores = new ArrayList<>();
        afterPropertiesSet();
    }

    private void afterPropertiesSet(){
        //message store initial
        defaultMessageStores.add(new MemoryMessageStore());
        binlogMessageStores.add(new RdbBinlogMessageStore(dataSource));
        binlogMessageStores.add(new MemoryMessageStore());

        //meta store initial
        metaStores.add(new MemoryMetaStore());
        metaStores.add(new RdbMetaStore(springContext));

        //positionOffset Store initial
        positionOffsetStores.add(new MemoryPositionOffsetStore());
        positionOffsetStores.add(new RdbPositionOffsetStore(dataSource));
    }

    public MessageStore getMessageStore(String topicType) {
        switch (TopicType.getEnum(topicType)){
            case Binlog:
                return binlogMessageStores.stream().filter(x->x.storageType(messageStoreType))
                        .findAny().orElseThrow(()->new MqServerException("Binlog topicType not found message store:"+messageStoreType));

            case Default:
                return defaultMessageStores.stream().filter(x->x.storageType(messageStoreType))
                        .findAny().orElse(defaultMessageStores.get(0));
            default:
                throw new RuntimeException("topicType not support :{}"+topicType);
        }
    }

    public MetaStore getMetaStore() {
        return metaStores.stream().filter(x->x.storageType(metaStoreType))
                .findAny().orElseThrow(()->new MqServerException("not found meta store:"+metaStoreType));
    }

    public PositionOffsetStore getPositionOffsetStore() {
        return positionOffsetStores.stream().filter(x->x.storageType(messageStoreType))
                .findAny().orElseThrow(()->new MqServerException("not found PositionOffset Store:"+messageStoreType));
    }
}
