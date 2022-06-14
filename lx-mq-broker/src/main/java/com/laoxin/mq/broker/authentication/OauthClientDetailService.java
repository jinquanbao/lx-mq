package com.laoxin.mq.broker.authentication;


import com.laoxin.mq.broker.entity.mq.OauthClientDetailsEntity;

import java.util.Optional;

public interface OauthClientDetailService {

    Optional<OauthClientDetailsEntity> getCacheByClientId(String clientId);

    void clear(String clientId);
}
