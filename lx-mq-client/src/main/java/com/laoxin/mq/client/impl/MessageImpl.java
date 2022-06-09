package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageId;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MessageImpl<T> implements Message {

    private MessageIdImpl messageId;

    private T payload;

    transient private Map<String, Object> properties;


    public MessageImpl(MessageIdImpl messageId, T payload, Map<String, Object> properties) {
        this.messageId = messageId;
        this.payload = payload;
        this.properties = properties;
    }

    MessageImpl(){

    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public MessageId getMessageId() {
        return messageId;
    }

    @Override
    public T getValue() {
        return payload;
    }



}
