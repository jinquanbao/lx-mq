package com.laoxin.mq.broker.entity.mq.trace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProducerLogEntity {

    private String producerName;

    private String producerAddress;

    private Long msgCreateTime;

    private Long msgStoredTime;

    public ProducerLogEntity merge(ProducerLogEntity entity){
        if(entity == null){
            return this;
        }
        if(entity.getMsgCreateTime() != null && entity.getMsgCreateTime()>0){
            this.msgCreateTime = entity.getMsgCreateTime();
        }
        if(entity.getMsgStoredTime() != null && entity.getMsgStoredTime()>0){
            this.msgStoredTime = entity.getMsgStoredTime();
        }
        return this;
    }
}
