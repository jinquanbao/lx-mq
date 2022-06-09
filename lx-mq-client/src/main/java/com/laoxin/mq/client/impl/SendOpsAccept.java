package com.laoxin.mq.client.impl;

import com.laoxin.mq.client.exception.MqClientException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SendOpsAccept {

    private final Map<Long,SendOps> sendOpsMap;

    private final ScheduledExecutorService executorService;

    public SendOpsAccept(ScheduledExecutorService executorService){
        sendOpsMap = new ConcurrentHashMap<>();
        this.executorService = executorService;
        executorService.scheduleAtFixedRate(() -> {
                    sendOpsMap.forEach((seqId, ops) -> {
                        if (System.currentTimeMillis() > ops.getExpiredAt()) {
                            log.error("send time out,seqId={},sendOps={}",ops);
                            accept(seqId, null, new MqClientException.TimeOutException("send time out,seqId="+seqId));
                        }
                    });
                }, 60, 60, TimeUnit.SECONDS);
    }

    public void registerSendOps(Long seqId,SendOps ops){
        sendOpsMap.put(seqId,ops);
    }

    public void removeSendOps(Long seqId){
        sendOpsMap.remove(seqId);
    }

    public <T> boolean accept(Long seqId, T callbackMsg,Exception e){

        SendOps sendOps = sendOpsMap.get(seqId);
        if(sendOps != null ){
            sendOpsMap.remove(seqId);
            if(e == null && System.currentTimeMillis()>sendOps.getExpiredAt()){
                e = new MqClientException.TimeOutException("send time out,seqId="+seqId);
            }
            sendOps.getCallback().callback(callbackMsg,e);
            return true;
        }
        return false;
    }
}
