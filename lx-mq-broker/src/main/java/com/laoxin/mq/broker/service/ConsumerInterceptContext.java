package com.laoxin.mq.broker.service;

import com.laoxin.mq.client.api.Message;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public interface ConsumerInterceptContext {

    void prePull(Consumer consumer);

    void pullSuccess(Consumer consumer, List<Message> messages);

    void prePush(Consumer consumer);

    void pushSuccess(Consumer consumer, List<Message> messages);

    void ack(long entryId);

    void ack(List<Long> entryId);

    TreeMap<Long,MessageOut> getPushedMessages();
    TreeMap<Long,MessageOut>  getPulledMessages();

    CompletableFuture<Void> clear();

    CompletableFuture<Void> clearForce();
}
