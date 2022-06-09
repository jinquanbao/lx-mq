package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.SendCallback;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SendOps {

    private long createAt;

    private long expiredAt;

    private String commandType;

    SendCallback callback;

    @Override
    public String toString() {
        return "SendOps{" +
                "createAt=" + createAt +
                ", expiredAt=" + expiredAt +
                ", commandType='" + commandType + '\'' +
                '}';
    }
}
