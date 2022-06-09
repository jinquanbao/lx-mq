package com.laoxin.mq.broker.entity.mq;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * @author dao-helper
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@TableName("subscription")
public class SubscriptionEntity  {



    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "无")
    private Integer id;


    @ApiModelProperty(value = "租户id")
    private Long tenantId;


    @ApiModelProperty(value = "订阅名称")
    private String subscriptionName;


    @ApiModelProperty(value = "topic名称")
    private String topicName;


    @ApiModelProperty(value = "1-在线；0-下线")
    private Integer status;


    @ApiModelProperty(value = "订阅模式;shared-广播;direct-直连")
    private String type;


    @ApiModelProperty(value = "订阅者元数据信息")
    private String metaData;


    @Version
    private Integer version;

    private Integer deleted;

    @ApiModelProperty(value = "创建时间")
    private java.time.LocalDateTime createTime;


    @ApiModelProperty(value = "更新时间")
    private java.time.LocalDateTime updateTime;

    @ApiModelProperty(value = "删除时间")
    private java.time.LocalDateTime deleteTime;

}
