package com.lanf.rocketmq.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

/**
 * MQ发送消息分页查询
 */
@Data
public class MqSendMessagePageQuery extends PageQuery {

    /**
     * topic
     */
    private String topic;

    /**
     * 消息状态 0:待发送 1:发送成功
     */
    private Integer status;



}
