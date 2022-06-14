package com.laoxin.mq.broker.entity.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OauthClientDetailsEntity {



    private Long id;


    private String clientId;


    private String clientSecret;


    private String resourceIds;


    private String grantTypes;


    private String authorities;


    private String redirectUri;


    private Integer accessTokenValidity;


    private Integer refreshTokenValidity;


    private String additionalInformation;


    private String autoApproveScopes;


    private java.time.LocalDateTime createTime;


    private java.time.LocalDateTime updateTime;


    private Integer deleted;


    private String remark;


    private String scope;

    private Long tenantId;
}
