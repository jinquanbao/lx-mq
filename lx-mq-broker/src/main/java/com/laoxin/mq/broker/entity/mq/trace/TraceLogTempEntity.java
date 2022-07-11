package com.laoxin.mq.broker.entity.mq.trace;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private ProducerLogEntity producerLog;

    @ApiModelProperty(value = "订阅者轨迹日志")
    private List<SubscriptionLogEntity> subscriptionLog;

    public TraceLogTempEntity merge(TraceLogTempEntity old,TraceLogTempEntity update){
        TraceLogTempEntity ret = old;
        if(update.producerLog != null){
            ret.producerLog = update.producerLog;
        }
        ret.subscriptionLog = ret.subscriptionLog == null? new ArrayList<>():ret.subscriptionLog;

        if(update.subscriptionLog != null && !update.subscriptionLog.isEmpty()){
            ret.subscriptionLog.addAll(update.subscriptionLog);
            ret.subscriptionLog.stream()
                    .collect(Collectors.toMap(x->x.getSubscriptionName(),x->x,(k1,k2)->k1.merge(k1,k2)))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
        }

        return ret;
    }

}
