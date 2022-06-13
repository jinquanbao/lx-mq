package com.laoxin.mq.broker.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface MessageTagFilter {

    String TAGS_VALUE = "TAGS";

    boolean accept(Map<String,String> messageProperties);

    static Set<String> toMessageTags(Map<String,String> messageProperties){
        if(messageProperties== null ){
            return null;
        }
        Set<String> tags = new HashSet<>();
        messageProperties.forEach((k,v)->{
            if(TAGS_VALUE.equals(v)){
                tags.add(k);
            }
        });
        return tags;
    }
}
