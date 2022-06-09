package com.laoxin.mq.broker.exception;

public class MqServerException extends RuntimeException {

    public MqServerException(String msg) {
        super(msg);
    }

    public MqServerException(Throwable t) {
        super(t);
    }



}
