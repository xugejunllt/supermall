package com.lanf.rocketmq.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.rocketmq.model.enums.MqSendMessageTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */

@Data
@TableName("mq_send_message")
public class MqSendMessageDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private String topic;

    private MqSendMessageTypeEnum sendMessageType;

    private String tag;

    @ApiModelProperty(value = "延迟时间 单位分钟")
    private Integer delayTime;

    @ApiModelProperty(value = "如果是顺序消息，消息key")
    private String messageKey;

    @ApiModelProperty(value = "消息内容")
    private String messageContent;

    @ApiModelProperty(value = "0:待发送 1:发送成功 ")
    private Integer status;

    @ApiModelProperty(value = "重试次数")
    private Integer retryCount;
    /**
     * 下次重试完成时间 用于心跳检测
     */
    private Date nextEstimatedCompletionAt;
    /**
     * 分片键
     */
    private String shardingKey ;

}
