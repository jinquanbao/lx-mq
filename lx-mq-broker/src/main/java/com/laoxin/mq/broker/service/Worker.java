package com.laoxin.mq.broker.service;

import java.util.concurrent.CompletableFuture;

public interface Worker {

    void start();

    CompletableFuture<Void> close();
}
