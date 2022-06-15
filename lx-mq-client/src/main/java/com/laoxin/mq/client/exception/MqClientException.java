package com.laoxin.mq.client.exception;

import com.laoxin.mq.client.enums.ResultErrorEnum;
import lombok.Getter;

import java.io.IOException;

@Getter
public class MqClientException extends IOException {

    private String code;

    public MqClientException(String msg) {
        super(msg);
        this.code = ResultErrorEnum.FAILED.getCode();
    }

    public MqClientException(String code,String msg) {
        super(msg);
        this.code = code;
    }

    public MqClientException(ResultErrorEnum error) {
        this(error.getCode(), error.getMessage());
    }

    public MqClientException(ResultErrorEnum error,String msg) {
        this(error.getCode(), msg);
    }

    public MqClientException(Throwable t) {
        super(t);
        this.code = ResultErrorEnum.FAILED.getCode();
    }

    public static class ConnectException extends MqClientException {
        public ConnectException(String msg) {
            super(msg);
        }
    }

    public static class NotConnectedException extends MqClientException {
        public NotConnectedException(String msg) {
            super(msg);
        }
    }

    public static class TimeOutException extends MqClientException {
        public TimeOutException(String msg) {
            super(msg);
        }
    }

    public static class AlreadyClosedException extends MqClientException {
        public AlreadyClosedException(String msg) {
            super(msg);
        }
    }

    public static class InvalidConfigurationException extends MqClientException {
        public InvalidConfigurationException(String msg) {
            super(msg);
        }
    }

    public static MqClientException translateException(Throwable t){
        if(t instanceof MqClientException){
            return (MqClientException)t;
        }
        if(t.getCause() instanceof MqClientException){
            return (MqClientException)t.getCause();
        }
        return new MqClientException(t);
    }
}
