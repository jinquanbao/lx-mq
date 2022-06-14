package com.laoxin.mq.broker.authentication;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ClientDetails extends Serializable {

    long getTenantId();

    String getClientId();

    Set<String> getResourceIds();

    boolean isSecretRequired();

    String getClientSecret();

    boolean isScoped();

    Set<String> getScope();

    Set<String> getAuthorizedGrantTypes();

    Set<String> getRegisteredRedirectUri();

    Collection<String> getAuthorities();

    Integer getAccessTokenValiditySeconds();

    Integer getRefreshTokenValiditySeconds();

    boolean isAutoApprove(String var1);

    Map<String, Object> getAdditionalInformation();

}
