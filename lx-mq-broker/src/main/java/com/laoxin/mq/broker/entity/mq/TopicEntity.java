package com.laoxin.mq.broker.entity.mq;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("topic")
public class TopicEntity  {



    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "无")
    private Integer id;


    @ApiModelProperty(value = "租户id")
    private Long tenantId;


    @ApiModelProperty(value = "主题名称")
    private String topicName;


    @ApiModelProperty(value = "binlog")
    private String type;


    @ApiModelProperty(value = "topic元数据信息")
    private String metaData;


    @ApiModelProperty(value = "是否可用1-是;0-否")
    private Integer enable;


    @ApiModelProperty(value = "创建时间")
    private java.time.LocalDateTime createTime;


    @ApiModelProperty(value = "更新时间")
    private java.time.LocalDateTime updateTime;




}
