package com.laoxin.mq.broker.authentication;

import com.laoxin.mq.broker.config.BrokerConfigurationData;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class DefaultAuthenticationProviderManager implements AuthenticationProviderManager{

    private final List<AuthenticationProvider> providers;
    private final DataSource dataSource;

    public DefaultAuthenticationProviderManager(DataSource dataSource, BrokerConfigurationData conf){
        this.dataSource = dataSource;
        this.providers = new ArrayList<>();
        afterPropertiesSet();
    }

    private void afterPropertiesSet(){
        this.providers.add(new CredentialsAuthenticationProvider(new OauthClientDetailServiceImpl(dataSource)));
    }


    @Override
    public List<AuthenticationProvider> providers() {
        return providers;
    }
}
