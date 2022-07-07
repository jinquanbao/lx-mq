package com.laoxin.mq.broker.service;

import com.laoxin.mq.broker.stats.ProducerStatsImpl;
import com.laoxin.mq.broker.stats.ProducerStatsRecorder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class Producer {

    private final MqServerHandler sh;

    private final Topic topic;

    private ProducerKey producerKey;

    private String producerName;

    private final CompletableFuture<Void> closeFuture;

    private final ProducerStatsRecorder stats;

    private final boolean enableMonitor;

    public Producer(MqServerHandler sh,Topic topic,ProducerKey producerKey,String producerName){
        this.sh = sh;
        this.topic = topic;
        this.producerKey = producerKey;
        this.producerName = producerName;
        this.closeFuture = new CompletableFuture<>();
        this.enableMonitor = ((TopicImpl)topic).brokerConf().isEnableMonitor();
        this.stats = enableMonitor?new ProducerStatsImpl(producerName,producerKey.getTenantId(),System.currentTimeMillis()):ProducerStatsRecorder.disabledInstance();
    }

    CompletableFuture<Long> publishMessage(String message){

        CompletableFuture<Long> future = new CompletableFuture<>();

        topic.publishMessage(message,(e,v)->{
            if(e != null){
                future.completeExceptionally(e);
            }else {
                future.complete(v);
            }
            updateStats(e);
        });

        return future;
    }

    private void updateStats(Exception e){
        if(e != null){
            stats.setException(e);
            stats.setExceptionTimestamp(System.currentTimeMillis());
        }else {
            stats.setException(null);
            stats.incrementMsgInCounter(1);
            stats.setLastMsgInTimestamp(System.currentTimeMillis());
        }
    }

    public ProducerStatsRecorder getStats(){
        stats.intervalCalculate();
        return stats;
    }

    public ProducerKey getProducerKey() {
        return producerKey;
    }

    public Topic getTopic(){
        return topic;
    }

    public String getProducerName(){
        return producerName;
    }

    public CompletableFuture<Void> disconnect() {
        log.info("Disconnecting producer: {}", this);
        //sh.closeProducer(this);
        try {
            close();
        } catch (Exception e) {
            log.warn("Consumer {} was already closed: {}", this, e.getMessage(), e);
        }
        return closeFuture;
    }

    public CompletableFuture<Void> close(){
        if (log.isDebugEnabled()) {
            log.debug("Closing producer {}", this);
        }
        topic.removeProducer(this);
        sh.removeProducer(this);
        closeFuture.complete(null);
        return closeFuture;
    }



    @Override
    public String toString() {
        return "Producer{" +
                "producerKey=" + producerKey +
                ", producerName='" + producerName + '\'' +
                '}';
    }
}
