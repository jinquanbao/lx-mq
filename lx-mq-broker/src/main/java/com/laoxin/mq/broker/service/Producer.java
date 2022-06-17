package com.laoxin.mq.broker.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class Producer {

    private final MqServerHandler sh;

    private final Topic topic;

    private ProducerKey producerKey;

    private String producerName;

    private final CompletableFuture<Void> closeFuture;

    public Producer(MqServerHandler sh,Topic topic,ProducerKey producerKey,String producerName){
        this.sh = sh;
        this.topic = topic;
        this.producerKey = producerKey;
        this.producerName = producerName;
        this.closeFuture = new CompletableFuture<>();
    }

    CompletableFuture<Long> publishMessage(String message){

        CompletableFuture<Long> future = new CompletableFuture<>();

        topic.publishMessage(message,(e,v)->{
            if(e != null){
                future.completeExceptionally(e);
            }else {
                future.complete(v);
            }
        });

        return future;
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
