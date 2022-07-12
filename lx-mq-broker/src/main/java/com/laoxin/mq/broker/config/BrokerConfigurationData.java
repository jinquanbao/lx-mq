package com.laoxin.mq.broker.config;

import com.laoxin.mq.broker.enums.StoreType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Getter
@Setter
@ConfigurationProperties(prefix = "mq")
@Configuration
public class BrokerConfigurationData implements Serializable {

    private int serverPort = 17000;

    //消息存储类型
    private String messageStoreType = StoreType.memory.name();

    //元数据存储类型
    private String metaStoreType = StoreType.memory.name();

    //缓存消费队列阈值
    private int consumerQueueThresholdSzie = 512;

    //默认默认每次推送消息数量
    private int defaultPushSize = 10;

    //消息读取任务线程数
    private int readMessageThreads = 50;

    //消息推送线程数
    private int pushMessageThreads = 20;

    //是否开启消息监控
    private boolean enableMonitor = false;
    //是否开启消息追踪
    private boolean enableTrace = false;
}
