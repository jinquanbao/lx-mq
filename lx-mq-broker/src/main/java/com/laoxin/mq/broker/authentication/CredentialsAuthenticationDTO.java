package com.laoxin.mq.broker.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CredentialsAuthenticationDTO implements AuthenticationDTO{
    private String clientId;

    public static void main(String[] args) {
        AuthenticationDTO dto = CredentialsAuthenticationDTO.builder().build();

        final Class<? extends AuthenticationDTO> authentication = dto.getClass();

        System.out.println(CredentialsAuthenticationDTO.class.isAssignableFrom(authentication));
    }
}
