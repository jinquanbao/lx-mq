package com.laoxin.mq.client.conf;

import com.laoxin.mq.client.enums.TopicType;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProducerConfigurationData implements Serializable {

    private String topic;

    private TopicType topicType = TopicType.Binlog;

    private String produceName;

    private long sendTimeoutMs = 30000L;

    private long tenantId;

}
