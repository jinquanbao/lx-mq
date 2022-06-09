package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.position.PositionOffsetStore;

public interface StoreManager {

    MessageStore getMessageStore(String topicType);

    MetaStore getMetaStore();

    PositionOffsetStore getPositionOffsetStore();
}
