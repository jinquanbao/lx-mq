package com.laoxin.mq.broker.authentication;

import com.laoxin.mq.broker.config.BrokerConfigurationData;
import com.laoxin.mq.broker.enums.StoreType;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class DefaultAuthenticationProviderManager implements AuthenticationProviderManager{

    private final List<AuthenticationProvider> providers;
    private final DataSource dataSource;
    private final String metaStoreType;

    public DefaultAuthenticationProviderManager(DataSource dataSource, BrokerConfigurationData conf){
        this.dataSource = dataSource;
        this.providers = new ArrayList<>();
        this.metaStoreType = conf.getMetaStoreType();
        afterPropertiesSet();
    }

    private void afterPropertiesSet(){
        ClientDetailsService clientDetailsService = null;
        switch (StoreType.getEnum(metaStoreType)){
            case memory:
                clientDetailsService = new MemoryClientDetailsServiceImpl();
                break;
            default:
                clientDetailsService = new OauthClientDetailServiceImpl(dataSource);
        }
        this.providers.add(new CredentialsAuthenticationProvider(clientDetailsService));
    }


    @Override
    public List<AuthenticationProvider> providers() {
        return providers;
    }
}
