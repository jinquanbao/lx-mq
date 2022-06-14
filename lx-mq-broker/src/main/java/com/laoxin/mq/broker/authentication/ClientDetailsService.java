package com.laoxin.mq.broker.authentication;

public interface ClientDetailsService {

    ClientDetails loadClientByClientId(String var1);
}
