package com.laoxin.mq.broker.authentication;

import com.google.common.collect.Sets;

public class MemoryClientDetailsServiceImpl implements ClientDetailsService{
    @Override
    public ClientDetails loadClientByClientId(String var1) {
        return DefaultClientDetails.builder()
                .clientId(var1)
                .scope(Sets.newHashSet("all"))
                .tenantId(1)
                .build();
    }
}
