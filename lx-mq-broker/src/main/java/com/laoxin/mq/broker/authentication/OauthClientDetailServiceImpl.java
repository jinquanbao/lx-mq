package com.laoxin.mq.broker.authentication;

import com.laoxin.mq.broker.entity.mq.OauthClientDetailsEntity;
import com.laoxin.mq.client.util.JSONUtil;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.*;

public class OauthClientDetailServiceImpl implements OauthClientDetailService,ClientDetailsService{
    private static Map<String,OauthClientDetailsEntity> cacheMap = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;

    final String GET_BY_CLIENT_ID = "select * from oauth_client_details where deleted = 0 and client_id = ?";

    public OauthClientDetailServiceImpl(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<OauthClientDetailsEntity> getCacheByClientId(String clientId) {

        OauthClientDetailsEntity entity = cacheMap.get(clientId);
        if(entity != null){
            return Optional.of(entity);
        }

        synchronized (cacheMap){

            entity = cacheMap.get(clientId);
            if(entity != null){
                return Optional.of(entity);
            }

            final Optional<OauthClientDetailsEntity> optional = getByClientId(clientId);
            if(optional.isPresent()){
                cacheMap.put(clientId,optional.get());
            }

            return optional;
        }
    }

    public Optional<OauthClientDetailsEntity> getByClientId(String clientId){

        List<OauthClientDetailsEntity> result = jdbcTemplate.query(GET_BY_CLIENT_ID, new BeanPropertyRowMapper(OauthClientDetailsEntity.class),clientId);

        return (result == null || result.isEmpty())?Optional.empty():Optional.of(result.get(0));
    }

    @Override
    public void clear(String clientId) {

        synchronized (cacheMap){

            if(!StringUtils.hasText(clientId)){
                cacheMap.clear();
            }else {
                cacheMap.remove(clientId);
            }

        }

    }

    public ClientDetails loadClientByClientId(String clientId) {

        final Optional<OauthClientDetailsEntity> optional = getCacheByClientId(clientId);
        if(optional.isPresent()){
            return convert(optional.get());
        }

        return null;
    }

    private ClientDetails  convert(OauthClientDetailsEntity e){
        return DefaultClientDetails.builder()
                .clientId(e.getClientId())
                .clientSecret(e.getClientSecret())
                .resourceIds(StringUtils.hasText(e.getResourceIds())? StringUtils.commaDelimitedListToSet(e.getResourceIds()):new HashSet<>())
                .grantTypes(StringUtils.hasText(e.getGrantTypes())? StringUtils.commaDelimitedListToSet(e.getGrantTypes()):new HashSet<>())
                .authorities(StringUtils.hasText(e.getAuthorities())? StringUtils.commaDelimitedListToSet(e.getAuthorities()):new HashSet<>())
                .redirectUri(StringUtils.hasText(e.getRedirectUri())? StringUtils.commaDelimitedListToSet(e.getRedirectUri()):new HashSet<>())
                .accessTokenValidity(e.getAccessTokenValidity())
                .refreshTokenValidity(e.getRefreshTokenValidity())
                .additionalInformation(StringUtils.hasText(e.getAdditionalInformation())? JSONUtil.fromJson(e.getAdditionalInformation(),HashMap.class) :new HashMap<>())
                .autoApproveScopes(StringUtils.hasText(e.getAutoApproveScopes())? StringUtils.commaDelimitedListToSet(e.getAutoApproveScopes()):new HashSet<>())
                .scope(StringUtils.hasText(e.getScope())? StringUtils.commaDelimitedListToSet(e.getScope()):new HashSet<>())
                .tenantId(e.getTenantId() == null?0:e.getTenantId() )
                .build();
    }
}
