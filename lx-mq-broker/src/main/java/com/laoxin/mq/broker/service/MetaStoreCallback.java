package com.laoxin.mq.broker.service;

public interface MetaStoreCallback<T> {

    void complete(T meta,Exception e);


}
