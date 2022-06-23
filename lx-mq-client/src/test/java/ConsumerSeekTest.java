import com.laoxin.mq.client.api.Consumer;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageId;
import com.laoxin.mq.client.api.MqClient;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class ConsumerSeekTest {

    public static void main(String[] args) throws MqClientException {

        final MqClient mqclient = MqClient.builder()
                .authClientId("testclient")
                .listenerThreads(1)
                .serviceUrl("tcp://127.0.0.1:17000")
                .build();

        final Consumer<String> consumer = mqclient.newConsumer()
                .consumerName("consumer_SEEK")
                .subscriptionName("consumer_pull")
                .topic("test")
                .topicType(TopicType.Default)
                .subscriptionType(SubscriptionType.Shared)
                .subscribe();

        final Consumer<String> consumer2 = mqclient.newConsumer()
                .consumerName("consumer1")
                .subscriptionName("consumer_push")
                .topic("test2")
                .topicType(TopicType.Default)
                .subscriptionType(SubscriptionType.Shared)
                .ackTimeOut(10, TimeUnit.SECONDS)
                .subscriptionProperty("tagTest","v1")
                .subscribe();

        Scanner scanner = new Scanner(System.in);


        while (scanner.hasNext()){

            try {
                final String next = scanner.next();
                consumer.seek(MessageId.from("test",1,Long.parseLong(next)));
                consumer2.seek(MessageId.from("test2",1,Long.parseLong(next)));
                System.out.println("seek success");
            }catch (Exception e){
                System.out.println("seek error"+e.getMessage());
                e.printStackTrace();
            }

        }

    }


}
