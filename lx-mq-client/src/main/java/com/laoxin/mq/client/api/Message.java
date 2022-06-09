package com.laoxin.mq.client.api;

import java.util.List;
import java.util.Map;

public interface Message<T> {

    Map<String, Object> getProperties();

    Object getProperty(String name);

    MessageId getMessageId();

    T getValue();

}
