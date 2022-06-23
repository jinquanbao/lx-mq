import com.laoxin.mq.client.api.Consumer;
import com.laoxin.mq.client.api.Message;
import com.laoxin.mq.client.api.MessageListener;
import com.laoxin.mq.client.api.MqClient;
import com.laoxin.mq.client.enums.SubscriptionType;
import com.laoxin.mq.client.enums.TopicType;
import com.laoxin.mq.client.exception.MqClientException;

import java.util.concurrent.TimeUnit;

public class ConsumerPushTest2 {

    public static void main(String[] args) throws MqClientException {

        final MqClient mqclient = MqClient.builder()
                .authClientId("testclient")
                .listenerThreads(1)
                .serviceUrl("tcp://127.0.0.1:17000")
                .build();

        final Consumer<String> consumer = mqclient.newConsumer()
                .consumerName("consumer2")
                .subscriptionName("consumer_push")
                .topic("test2")
                .topicType(TopicType.Default)
                .subscriptionType(SubscriptionType.Direct)
                .ackTimeOut(10, TimeUnit.SECONDS)
                .messageListener(new PushMessageListener())
                .subscribe();




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
