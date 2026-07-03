package com.lanf.rocketmq.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

import java.util.Date;

/**
 * MQ消费消息分页查询
 */
@Data
public class MqConsumeMessagePageQuery extends PageQuery {

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * topic
     */
    private String topic;

    /**
     * group
     */
    private String group;

    /**
     * 消息状态 0:待消费 1:消费成功 2:消费失败
     */
    private Integer status;



}
