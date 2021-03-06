import com.laoxin.mq.client.api.*;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ConsumerPushTest {

    public static void main(String[] args) throws MqClientException {

        final MqClient mqclient = MqClient.builder()
                .authClientId("testclient1")
                .listenerThreads(1)
                .serviceUrl("tcp://127.0.0.1:17000")
                .build();

        final Consumer<String> consumer = mqclient.newConsumer()
                .consumerName("consumer1")
                .subscriptionName("consumer_push")
                .topic("test2")
                .topicType(TopicType.Default)
                .subscriptionType(SubscriptionType.Direct)
                .ackTimeOut(10, TimeUnit.SECONDS)
                .messageListener(new PushMessageListener())
                .subscriptionProperty("tagTest","v1")
                .subscribe();

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()){

            try {
                final String next = scanner.next();
                consumer.seek(MessageId.from("test2",1,Long.parseLong(next)));
            }catch (Exception e){
                System.out.println("seek error"+e.getMessage());
                e.printStackTrace();
            }

        }


    }

    static class PushMessageListener implements MessageListener<String>{

        @Override
        public void onMessage(Consumer consumer, Message<String> msg) {
            System.out.println("consumer_push 收到消息id="+msg.getMessageId()+",value="+msg.getValue());
            try {
                consumer.ack(msg);
            } catch (MqClientException e) {
                e.printStackTrace();
            }
        }
    }
}
