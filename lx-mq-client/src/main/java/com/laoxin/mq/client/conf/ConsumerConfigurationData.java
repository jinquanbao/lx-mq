
package com.laoxin.mq.client.conf;

import com.laoxin.mq.client.api.MessageFilter;
import com.laoxin.mq.client.api.MessageListener;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;


@Data
public class ConsumerConfigurationData<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subscriptionName;

    //依赖的订阅名称，如果有设置，该订阅只能拉取依赖订阅ack后的数据
    private String dependencyOnSubscription;

    private SubscriptionType subscriptionType = SubscriptionType.Direct;

    private MessageListener<T> messageListener;

    //消息过滤,此为客户端消息过滤
    private MessageFilter filter;

    private String consumerName;
    //ack 超时时间,如果是push模式，超过此时间未ack，broker会重新推送
    private long ackTimeoutMillis = 0;

    private String topic;

    private TopicType topicType;

    private long tenantId;

    //消息过滤EL表达式,此为客户端消息过滤
    private String filterExpression;

    //消息标签过滤参数,key->tag名称;value->tag值,,此为服务端消息过滤
    Map<String, String> subscriptionProperties;

    private int listenerThreads = 1;

    private boolean orderConsumer = true;
}
