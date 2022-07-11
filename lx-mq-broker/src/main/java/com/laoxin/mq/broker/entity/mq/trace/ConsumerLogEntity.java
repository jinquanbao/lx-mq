package com.laoxin.mq.broker.entity.mq.trace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConsumerLogEntity {

    //消费者名称
    private String consumerName;

    //消费者地址
    private String address;

    //消息最后推送时间
    private Long msgOutTimestamp;

    //消费状态 0-未消费 1-已消费
    private int status;

    //确认消费时间
    private Long ackTimestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumerLogEntity that = (ConsumerLogEntity) o;
        return Objects.equals(consumerName, that.consumerName) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumerName, address);
    }

}
