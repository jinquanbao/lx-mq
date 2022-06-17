package com.laoxin.mq.client.api;


import com.laoxin.mq.client.impl.MessageIdImpl;

public interface MessageId extends Comparable<MessageId> {

    String getTopic();

    long getTenantId();

    long getEntryId();

    static MessageId from(String topic,long tenantId,long entryId){
        return new MessageIdImpl(tenantId,topic,entryId);
    }

}
