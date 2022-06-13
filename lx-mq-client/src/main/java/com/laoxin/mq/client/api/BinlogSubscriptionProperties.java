package com.laoxin.mq.client.api;

import com.laoxin.mq.client.impl.BinlogSubscriptionPropertiesImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface BinlogSubscriptionProperties extends Map<String,String> {

    static BinlogSubscriptionProperties newSubscriptionProperties(){
        return new BinlogSubscriptionPropertiesImpl(2);
    }

    static BinlogSubscriptionProperties from(Map<String,String> properties){
        if(properties == null){
            throw new NullPointerException("properties from which is marked non-null but is null");
        }
        return new BinlogSubscriptionPropertiesImpl(properties);
    }

    BinlogSubscriptionProperties tableNames(Collection<String> tableNames);

    BinlogSubscriptionProperties eventTypes(Collection<String> eventTypes);

    Set<String> getTableNames();

    Set<String> getEventTypes();



}
