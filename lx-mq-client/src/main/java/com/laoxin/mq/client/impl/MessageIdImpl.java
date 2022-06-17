package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.MessageId;
import lombok.Getter;

import java.util.Objects;

@Getter
public class MessageIdImpl implements MessageId {

    private long tenantId;
    private String topic;
    private long entryId;


    public MessageIdImpl(long tenantId, String topic,  long entryId){
        this.tenantId = tenantId;
        this.topic = topic;
        this.entryId = entryId;
    }

    MessageIdImpl(){

    }

    @Override
    public String toString() {
        return "MessageIdImpl{" +
                "tenantId='" + tenantId + '\'' +
                ", topic='" + topic + '\'' +
                ", entryId=" + entryId +
                '}';
    }

    @Override
    public int compareTo(MessageId o) {
        return Long.compare(entryId,o.getEntryId());
    }

    @Override
    public String getTopic() {
        return topic;
    }

}
