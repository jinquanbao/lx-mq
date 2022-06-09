package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.position.Position;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.impl.MessageIdImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageStore {

    boolean storageType(String storageType);

    CompletableFuture<List<Message>> readMessage(Position position, Long maxEntryId , int readSize);

    MessageIdImpl writeMessage(TopicKey topicKey,String message);
}
