package com.laoxin.mq.broker.exception;

import com.laoxin.mq.client.enums.ResultErrorEnum;
import lombok.Getter;

@Getter
public class MqServerException extends RuntimeException {

    private String code;

    public MqServerException(String msg) {
        super(msg);
        this.code = ResultErrorEnum.FAILED.getCode();
    }

    public MqServerException(String code,String msg) {
        super(msg);
        this.code = code;
    }

    public MqServerException(ResultErrorEnum error) {
        this(error.getCode(), error.getMessage());
    }

    public MqServerException(ResultErrorEnum error,String msg) {
        this(error.getCode(), msg);
    }

    public MqServerException(Throwable t) {
        super(t);
        this.code = ResultErrorEnum.FAILED.getCode();
    }

    public static MqServerException translateException(Throwable t){
        if(t instanceof MqServerException){
            return (MqServerException)t;
        }
        if(t.getCause() instanceof MqServerException){
            return (MqServerException)t.getCause();
        }
        return new MqServerException(t);
    }



}
