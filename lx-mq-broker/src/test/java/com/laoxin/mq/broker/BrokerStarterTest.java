package com.laoxin.mq.broker;

import com.alibaba.druid.pool.DruidDataSource;
import com.laoxin.mq.broker.config.BrokerConfigurationData;
import com.laoxin.mq.broker.service.BrokerService;
import com.laoxin.mq.broker.spring.DefaultSpringContext;
//消息存储、元数据存储以内存模式启动，方便调试测试
public class BrokerStarterTest {

    public static void main(String[] args) {
        new BrokerService(new DruidDataSource(),new DefaultSpringContext(null,null),new BrokerConfigurationData()).start();
    }
}
