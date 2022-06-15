package com.laoxin.mq.broker.service;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.Set;

@EqualsAndHashCode
public class DefaultMessageTagFilter implements MessageTagFilter{

    private final Map<String,String> subscriptionProperties;

    private boolean subscriptionAll = false;

    public DefaultMessageTagFilter(Map<String,String> subscriptionProperties){
        this.subscriptionProperties = subscriptionProperties;
        //this.subscriptionAll = subscriptionProperties == null || subscriptionProperties.isEmpty();
    }

    @Override
    public boolean accept(Map<String, String> messageProperties) {

        if(subscriptionProperties == null || subscriptionProperties.isEmpty()){
            return true;
        }

        final Set<String> tags = MessageTagFilter.toMessageTags(messageProperties);

        if(tags != null && !tags.isEmpty()){
            return subscriptionProperties.keySet().containsAll(tags);
        }

        return false;
    }



}
