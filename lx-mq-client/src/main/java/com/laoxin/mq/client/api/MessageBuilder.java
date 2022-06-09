package com.laoxin.mq.client.api;

import com.laoxin.mq.client.impl.MessageBuilderImpl;

import java.util.Map;

public interface MessageBuilder<T> {

    static MessageBuilder create() {
        return new MessageBuilderImpl();
    }

    Message build();

    MessageBuilder setContent(T data);

    MessageBuilder setProperties(Map<String, Object> properties);

    MessageBuilder setMessageId(MessageId messageId);

}
