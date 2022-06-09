package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageBuilder;
import com.laoxin.mq.client.api.MessageId;

import java.util.List;
import java.util.Map;

public class MessageBuilderImpl<T> implements MessageBuilder<T> {

    private final MessageImpl message;

    public MessageBuilderImpl(){
        message = new MessageImpl();
    }

    @Override
    public Message build() {
        return message;
    }

    @Override
    public MessageBuilder setContent(T data) {
        message.setPayload(data);
        return this;
    }

    @Override
    public MessageBuilder setProperties(Map<String, Object> properties) {
        message.setProperties(properties);
        return this;
    }

    @Override
    public MessageBuilder setMessageId(MessageId messageId) {
        message.setMessageId((MessageIdImpl) messageId);
        return this;
    }
}
