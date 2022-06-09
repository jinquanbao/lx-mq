package com.laoxin.mq.broker;

import com.laoxin.mq.broker.config.BrokerConfigurationData;
import com.laoxin.mq.broker.service.BrokerService;
import com.laoxin.mq.broker.spring.SpringContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;

import javax.sql.DataSource;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        new SpringApplicationBuilder(Application.class)
                .initializers((GenericApplicationContext c) -> c.setAllowBeanDefinitionOverriding(true) )
                .run(args);

    }

    @Bean(initMethod = "start",destroyMethod = "close")
    public BrokerService brokerService(DataSource dataSource, BrokerConfigurationData conf, SpringContext springContext){
        BrokerService brokerService = new BrokerService(dataSource,springContext,conf);
        return brokerService;
    }

}
