package com.laoxin.mq.client.conf;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ClientConfigurationData implements Serializable {

    //连接到broker或代理的url
    private String serviceUrl;

    //认证clientId
    private String authClientId;

    //消息监听线程数
    private int listenerThreads = 1;

    //最大连接数
    private int maxConnections = 1;


}
