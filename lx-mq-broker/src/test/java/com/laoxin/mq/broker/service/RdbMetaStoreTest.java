package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.Application;
import com.laoxin.mq.broker.entity.mq.SubscriptionConsumer;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class RdbMetaStoreTest {

    @Autowired
    private BrokerService brokerService;

    @Test
    public void storeTopicTest(){
        TopicMetaData topicMetaData = TopicMetaData.builder()
                .topicType(TopicType.Binlog.name())
                .topicName("rs_ssv")
                .tenantId(1)
                .build();

        brokerService.metaStore().storeTopic(topicMetaData,(v,e)->{
            System.out.println("topic store success");
        });

    }

    @Test
    public void storeSubscriptionTest(){
        TopicMetaData topicMetaData = TopicMetaData.builder()
                .topicType(TopicType.Binlog.name())
                .topicName("rs_ssv")
                .tenantId(1)
                .build();

        SubscriptionConsumer consumer = SubscriptionConsumer.builder()
                .address("127.0.0.1:11101")
                .consumerName("test2")
                .build();



        SubscriptionMetaData subscriptionMetaData = SubscriptionMetaData.builder()
                .consumer(consumer)
                .enablePush(true)
                .ackTimeOut(10000)
                .subscriptionType(SubscriptionType.Shared.name())
                .subscriptionName("subscriptionName")
                .build();

        brokerService.metaStore().storeSubscription(topicMetaData,subscriptionMetaData,(v,e)->{
            System.out.println("subscription store success");
        });

    }

    @Test
    public void removeSubscriptionTest(){
        SubscriptionKey subscriptionKey = SubscriptionKey.builder()
                .topicName("rs_ssv")
                .subscriptionName("subscriptionName")
                .tenantId(1L)
                .build();

        brokerService.metaStore().removeSubscription(subscriptionKey,(v,e)->{
            System.out.println("remove subscription success");
        });

    }

    @Test
    public void removeSubscriptionConsumerTest(){
        SubscriptionKey subscriptionKey = SubscriptionKey.builder()
                .topicName("rs_ssv")
                .subscriptionName("subscriptionName")
                .tenantId(1L)
                .build();

        SubscriptionConsumer consumer = SubscriptionConsumer.builder()
                .address("127.0.0.1:11100")
                .consumerName("test2")
                .build();

        brokerService.metaStore().removeSubscriptionConsumer(subscriptionKey,consumer,(v,e)->{
            System.out.println("remove subscription success");
        });

    }
}
