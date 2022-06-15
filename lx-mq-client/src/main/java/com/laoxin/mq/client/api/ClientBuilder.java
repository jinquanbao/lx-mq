package com.laoxin.mq.client.api;

import com.laoxin.mq.client.conf.ClientConfigurationData;
import com.laoxin.mq.client.impl.MqClientImpl;
import org.springframework.util.StringUtils;

public class ClientBuilder {

    private final ClientConfigurationData conf;

    public ClientBuilder(){
        this.conf =  new ClientConfigurationData();
    }

    public ClientBuilder(ClientConfigurationData conf){
        this.conf =  conf;
    }

    public MqClient build(){
        if(!StringUtils.hasText(conf.getServiceUrl())){
            throw new IllegalArgumentException("service URL needs to be specified on the ClientBuilder object.");
        }
        if(!StringUtils.hasText(conf.getAuthClientId())){
            throw new IllegalArgumentException("auth clientId needs to be specified on the ClientBuilder object.");
        }
        MqClient client = new MqClientImpl(conf);
        return client;
    }

    public ClientBuilder serviceUrl(String serviceUrl){
        this.conf.setServiceUrl(serviceUrl);
        return this;
    }

    public ClientBuilder authClientId(String authClientId){
        this.conf.setAuthClientId(authClientId);
        return this;
    }

    public ClientBuilder listenerThreads(int listenerThreads){
        this.conf.setListenerThreads(listenerThreads);
        return this;
    }

    public ClientBuilder maxConnections(int maxConnections){
        this.conf.setMaxConnections(maxConnections);
        return this;
    }

}
