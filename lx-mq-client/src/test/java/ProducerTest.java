import com.laoxin.mq.client.api.MessageId;
import com.laoxin.mq.client.api.MqClient;
import com.laoxin.mq.client.api.Producer;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProducerTest {

    public static void main(String[] args) throws MqClientException {

        final MqClient mqclient = MqClient.builder()
                .authClientId("testclient")
                .listenerThreads(1)
                .serviceUrl("127.0.0.1:17000")
                .maxConnections(1)
                .build();

        final Producer<String> producer = mqclient.newProducer()
                .producerName("producer1")
                .sendTimeout(10, TimeUnit.SECONDS)
                .topic("test")
                .topicType(TopicType.Default)
                .tenantId(1)
                .create();

        final Producer<String> producer2 = mqclient.newProducer()
                .producerName("producer2")
                .sendTimeout(10, TimeUnit.SECONDS)
                .topic("test2")
                .topicType(TopicType.Default)
                .tenantId(1)
                .create();

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()){
            try {
                final String next = scanner.next();

                CompletableFuture<MessageId> future = producer.sendAsync("producer2:"+next);

                MessageId sss = producer2.newMessage()
                        .property("tagTest","TAGS")
                        .value("producer1:"+next)
                        .send()
                        ;
                System.out.println("producer1 send success,ack MessageId="+sss);

                System.out.println("producer2 send success,ack MessageId="+future.get());
            }catch (Exception e){
                System.out.println("send error");
                e.printStackTrace();
            }
        }

    }



    static class User{
        public int id;
        public String name;
    }
}
