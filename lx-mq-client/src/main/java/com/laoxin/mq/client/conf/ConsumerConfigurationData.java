/**
 * Copyright 2016 Yahoo Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.laoxin.mq.client.conf;

import com.laoxin.mq.client.api.MessageFilter;
import com.laoxin.mq.client.api.MessageListener;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import lombok.Data;

import java.io.Serializable;


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

    //消息过滤EL表达式
    private String filterExpression;

    private int listenerThreads = 1;

    private boolean orderConsumer = true;
}
