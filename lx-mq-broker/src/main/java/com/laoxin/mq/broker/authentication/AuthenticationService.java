package com.laoxin.mq.broker.authentication;


public interface AuthenticationService {

    UserAuthContext authenticate(AuthenticationDTO authentication);
}
