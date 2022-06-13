package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.api.BinlogSubscriptionProperties;
import org.springframework.util.StringUtils;

import java.util.*;

public class BinlogSubscriptionPropertiesImpl extends HashMap<String,String> implements BinlogSubscriptionProperties {

    private final String TABLE_NAME_KEY = "table_name";
    private final String EVENT_TYPE_KEY = "event_type";

    public BinlogSubscriptionPropertiesImpl(int initialCapacity) {
        super(initialCapacity);
    }

    public BinlogSubscriptionPropertiesImpl() {
    }

    public BinlogSubscriptionPropertiesImpl(Map<? extends String, ? extends String> m) {
        super(m);
    }


    @Override
    public BinlogSubscriptionProperties tableNames(Collection<String> tableNames) {
        super.put(TABLE_NAME_KEY,StringUtils.collectionToDelimitedString(tableNames,","));
        return this;
    }

    @Override
    public BinlogSubscriptionProperties eventTypes(Collection<String> eventTypes) {
        super.put(EVENT_TYPE_KEY,StringUtils.collectionToDelimitedString(eventTypes,","));
        return this;
    }

    @Override
    public Set<String> getTableNames() {
        final String s = super.get(TABLE_NAME_KEY);
        if(s == null){
            new HashSet<>();
        }
        return StringUtils.commaDelimitedListToSet(s);
    }

    @Override
    public Set<String> getEventTypes() {
        final String s = super.get(EVENT_TYPE_KEY);
        if(s == null){
            new HashSet<>();
        }
        return StringUtils.commaDelimitedListToSet(s);
    }

}
