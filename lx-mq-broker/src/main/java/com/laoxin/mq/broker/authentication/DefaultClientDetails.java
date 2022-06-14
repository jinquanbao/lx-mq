package com.laoxin.mq.broker.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DefaultClientDetails implements ClientDetails{

    private String clientId;


    private String clientSecret;


    private Set<String> resourceIds;


    private Set<String> grantTypes;


    private Set<String> authorities;


    private Set<String> redirectUri;


    private Integer accessTokenValidity;


    private Integer refreshTokenValidity;


    private Map<String,Object> additionalInformation;


    private Set<String> autoApproveScopes;

    private Set<String> scope;

    private long tenantId;

    public boolean isSecretRequired() {
        return this.clientSecret != null;
    }

    @Override
    public boolean isScoped() {
        return this.scope != null && !this.scope.isEmpty();
    }

    @Override
    public Set<String> getAuthorizedGrantTypes() {
        return grantTypes;
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return redirectUri;
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return accessTokenValidity;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return refreshTokenValidity;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        if (this.autoApproveScopes == null) {
            return false;
        } else {
            Iterator var2 = this.autoApproveScopes.iterator();

            String auto;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                auto = (String)var2.next();
            } while(!auto.equals("true") && !scope.matches(auto));

            return true;
        }
    }
}
