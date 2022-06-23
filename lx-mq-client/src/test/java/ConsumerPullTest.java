import com.laoxin.mq.client.api.Consumer;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageId;
import com.laoxin.mq.client.api.MqClient;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;

import java.util.List;
import java.util.Scanner;


public class ConsumerPullTest {

    public static void main(String[] args) throws MqClientException {

        final MqClient mqclient = MqClient.builder()
                .authClientId("testclient")
                .listenerThreads(1)
                .serviceUrl("tcp://127.0.0.1:17000")
                .build();

        final Consumer<String> consumer = mqclient.newConsumer()
                .consumerName("consumer1")
                .subscriptionName("consumer_pull")
                .topic("test")
                .topicType(TopicType.Default)
                .subscriptionType(SubscriptionType.Shared)
                .subscribe();

        Thread thread = new Thread(()->pull(consumer));
        thread.start();



    }

    private static void pull(Consumer<String> consumer){
        while (true){

            try {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<Message<String>> messages = consumer.pull();

                if(messages == null || messages.isEmpty()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    messages.forEach(msg->{
                        System.out.println("consumer_pull 收到消息id="+msg.getMessageId()+",value="+msg.getValue());
                        try {
                            consumer.ack(msg);
                        } catch (MqClientException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }catch (Exception e){
                System.out.println("consumer_pull error"+e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
