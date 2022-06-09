import com.laoxin.mq.client.api.Consumer;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MqClient;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.exception.MqClientException;

import java.util.List;


public class ConsumerPullTest {

    public static void main(String[] args) throws MqClientException {

        final MqClient mqclient = MqClient.builder()
                .authClientId("testclient")
                .listenerThreads(1)
                .serviceUrl("127.0.0.1:17000")
                .build();

        final Consumer<String> consumer = mqclient.newConsumer()
                .consumerName("consumer1")
                .subscriptionName("consumer_pull")
                .topic("test")
                .subscriptionType(SubscriptionType.Shared)
                .subscribe();

        while (true){

            try {
                List<Message<String>> messages = consumer.pull();

                if(messages == null || messages.isEmpty()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    messages.forEach(msg->{
                        System.out.println("consumer_pull 收到消息："+msg);
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
