package com.laoxin.mq.broker.entity.mq.trace;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TableName("trace_log")
public class TraceLogTempEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "无")
    private Integer id;

    @ApiModelProperty(value = "租户id")
    private Long tenantId;

    @ApiModelProperty(value = "主题名称")
    private String topicName;

    @ApiModelProperty(value = "消息id")
    private long messageId;

    @ApiModelProperty(value = "生产者轨迹日志")
    private ProducerLogEntity producer_log;

    @ApiModelProperty(value = "订阅者轨迹日志")
    private Set<SubscriptionLogEntity> subscription_log;

    public TraceLogTempEntity merge(TraceLogTempEntity entity){


        return this;
    }

}
