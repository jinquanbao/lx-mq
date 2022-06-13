package com.laoxin.mq.client.command;

import com.laoxin.mq.client.enums.CommandType;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.util.JSONUtil;

import java.util.Map;

public interface Commands {

    static BaseCommand newSuccess(){
        return BaseCommand.builder().commandType(CommandType.SUCCESS.name()).build();
    }

    static BaseCommand newError(String code,String msg){

        CommandError build = CommandError.builder()
                .code(code)
                .msg(msg)
                .build()
                ;


        return BaseCommand.builder()
                .commandType(CommandType.ERROR.name())
                .body(JSONUtil.toJson(build))
                .build();
    }

    static BaseCommand newPing(){
        return BaseCommand.builder().commandType(CommandType.PING.name()).build();
    }

    static BaseCommand newPong(){
        return BaseCommand.builder().commandType(CommandType.PONG.name()).build();
    }


    static BaseCommand newSubscribe(Map<String,String> subscriptionProperties, long tenantId, String topic, TopicType topicType, String subscription, long consumerId,
                                    SubscriptionType subType, String consumerName, String dependencyOnSubscription
                                    , long ackTimeOut, boolean enablePush
    ) {
        final CommandSubscribe build = CommandSubscribe.builder()
                .consumerId(consumerId)
                .consumerName(consumerName)
                .subscription(subscription)
                .dependencyOnSubscription(dependencyOnSubscription)
                .subscribeType(subType.name())
                .ackTimeOut(ackTimeOut)
                .enablePush(enablePush)
                .topic(topic)
                .tenantId(tenantId)
                .topicType(topicType == null?null:topicType.name())
                .subscriptionProperties(subscriptionProperties)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.SUBSCRIBE.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newUnSubscribe(String topic, String subscription, long consumerId) {
        final CommandUnSubscribe build = CommandUnSubscribe.builder()
                .consumerId(consumerId)
                .subscribe(subscription)
                .topic(topic).build();

        return BaseCommand.builder()
                .commandType(CommandType.UNSUBSCRIBE.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newAck(String topic, String subscription, long consumerId,long tenantId,long entryId) {
        final CommandAck build = CommandAck.builder()
                .consumerId(consumerId)
                .entryId(entryId)
                .subscription(subscription)
                .tenantId(tenantId)
                .topic(topic)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.ACK.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newCloseConsumer(String topic, String subscription, long consumerId) {
        final CommandCloseConsumer build = CommandCloseConsumer.builder()
                .consumerId(consumerId)
                .subscription(subscription)
                .topic(topic)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.CLOSE_CONSUMER.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newPull(String topic, String subscription, long consumerId,long entryId,int batchSize) {
        final CommandPull build = CommandPull.builder()
                .consumerId(consumerId)
                .subscription(subscription)
                .topic(topic)
                .entryId(entryId)
                .size(batchSize)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.PULL.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }



    static BaseCommand newCloseProducer(String topic, long producerId,long tenantId) {
        final CommandCloseProducer build = CommandCloseProducer.builder()
                .producerId(producerId)
                .topic(topic)
                .tenantId(tenantId)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.CLOSE_PRODUCER.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newProducer(String topic, TopicType topicType, long producerId, String producerName, long tenantId) {
        final CommandCreateProducer build = CommandCreateProducer.builder()
                .producerId(producerId)
                .topic(topic)
                .tenantId(tenantId)
                .producerName(producerName)
                .topicType(topicType.name())
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.PRODUCER.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newProducerSuccess(String producerName) {
        final CommandProducerSuccess build = CommandProducerSuccess.builder()
                .producerName(producerName)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.PRODUCER_SUCCESS.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newSend(String topic, long producerId,long tenantId,long createdAt,String message) {
        final CommandSend build = CommandSend.builder()
                .producerId(producerId)
                .topic(topic)
                .tenantId(tenantId)
                .createdAt(createdAt)
                .message(message)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.SEND.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newSendReceipt(long producerId,long tenantId,long entryId) {
        final CommandSendReceipt build = CommandSendReceipt.builder()
                .producerId(producerId)
                .tenantId(tenantId)
                .entryId(entryId)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.SEND_RECEIPT.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newPullReceipt(long consumerId,String payloadAndHeaders) {
        final CommandMessage build = CommandMessage.builder()
                .consumerId(consumerId)
                .payloadAndHeaders(payloadAndHeaders)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.PULL_RECEIPT.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }

    static BaseCommand newMessage(long consumerId,String payloadAndHeaders) {
        final CommandMessage build = CommandMessage.builder()
                .consumerId(consumerId)
                .payloadAndHeaders(payloadAndHeaders)
                .build();

        return BaseCommand.builder()
                .commandType(CommandType.MESSAGE.name())
                .body(JSONUtil.toJson(build))
                .build()
                ;
    }
}
