package com.laoxin.mq.broker.authentication;

import com.laoxin.mq.broker.exception.MqServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CredentialsAuthenticationProvider implements AuthenticationProvider<CredentialsAuthenticationDTO>{

    private final ClientDetailsService clientDetailsService;

    public CredentialsAuthenticationProvider(ClientDetailsService clientDetailsService){
        this.clientDetailsService = clientDetailsService;
    }

    @Override
    public UserAuthContext authenticate(CredentialsAuthenticationDTO authentication) {
        final ClientDetails clientDetails = clientDetailsService.loadClientByClientId(authentication.getClientId());
        if(clientDetails == null){
            log.error("client not exist");
            throw new MqServerException("client not found :"+authentication.getClientId());
        }
        return UserAuthContext.builder()
                .clientId(authentication.getClientId())
                .tenantId(clientDetails.getTenantId())
                .scope(clientDetails.getScope())
                .build();
    }

    @Override
    public boolean support(Class authentication) {
        return CredentialsAuthenticationDTO.class.isAssignableFrom(authentication);
    }
}
