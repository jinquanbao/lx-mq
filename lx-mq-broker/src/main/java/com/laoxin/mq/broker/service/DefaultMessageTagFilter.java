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
        this.subscriptionAll = subscriptionProperties != null && subscriptionProperties.containsKey("*");
    }

    @Override
    public boolean accept(Map<String, String> messageProperties) {

        final Set<String> tags = MessageTagFilter.toMessageTags(messageProperties);

        if(subscriptionAll){
            return true;
        }

        if((tags == null || tags.isEmpty()) && (subscriptionProperties == null || subscriptionProperties.isEmpty())){
            return true;
        }

        if(tags != null && !tags.isEmpty() && subscriptionProperties != null){
            return subscriptionProperties.keySet().containsAll(tags);
        }

        return false;
    }



}
