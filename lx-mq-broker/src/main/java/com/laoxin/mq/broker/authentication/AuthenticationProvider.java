package com.laoxin.mq.broker.authentication;


public interface AuthenticationProvider<T extends AuthenticationDTO> {

    UserAuthContext authenticate(T authentication);

    boolean support(Class<? extends AuthenticationDTO> authentication);
}
