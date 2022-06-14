package com.laoxin.mq.broker.authentication;

import com.laoxin.mq.broker.exception.MqServerException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService{

    private final List<AuthenticationProvider> providers;

    public AuthenticationServiceImpl(AuthenticationProviderManager manager){
        this.providers = manager.providers();
    }

    @Override
    public UserAuthContext authenticate(AuthenticationDTO dto) {
        for(AuthenticationProvider provider: providers){
            if(provider.support(dto.getClass())){
                return provider.authenticate(dto);
            }
        }
        log.error("Authentication class of AuthenticationProvider not implements: " + dto.getClass());
        throw new MqServerException("authenticate failed: Authentication class of AuthenticationProvider not implements:"+dto.getClass());
    }
}
