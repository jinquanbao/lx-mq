package com.laoxin.mq.client.exception;

import java.io.IOException;

public class MqClientException extends IOException {

    public MqClientException(String msg) {
        super(msg);
    }

    public MqClientException(Throwable t) {
        super(t);
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

}
