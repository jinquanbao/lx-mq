package com.laoxin.mq.client.api;

import com.laoxin.mq.client.enums.ConnectionState;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class AbstractConnectionStateHandler extends SimpleChannelInboundHandler {

    protected ConnectionState state = ConnectionState.None;

    protected boolean isConnected(){
        return state == ConnectionState.Connected;
    }

    protected void checkConnected(){
        if(!isConnected()){
            throw new IllegalArgumentException("conection is not ready");
        }
    }
}
