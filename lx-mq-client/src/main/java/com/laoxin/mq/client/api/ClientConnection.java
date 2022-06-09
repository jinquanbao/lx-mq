package com.laoxin.mq.client.api;

import com.laoxin.mq.client.exception.MqClientException;
import com.laoxin.mq.client.impl.MqClientHandler;

public interface ClientConnection {

    void connectionFailed(MqClientException e);

    void connectionOpened(MqClientHandler ch);

}
