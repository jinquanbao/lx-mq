package com.laoxin.mq.client.enums;

import lombok.Getter;

@Getter
public enum ResultErrorEnum {
    SUCCESS("0", "success"),
    FAILED("-1", "failed"),
    CONSUMER_EXCLUDE("10","directed consumer is already connected"),
    AUTH_FAILED("401", "鉴权失败"),
    AUTH_FORBIDDEN("403", "授权失败,禁止访问"),//Not authorized to use resource

    ;
    private String code;
    private String message;

    ResultErrorEnum(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String toString() {
        return this.code + "_" + this.message;
    }
}
