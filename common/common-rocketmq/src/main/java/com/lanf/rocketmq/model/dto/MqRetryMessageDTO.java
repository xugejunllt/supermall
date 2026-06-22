package com.lanf.rocketmq.model.dto;

import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import lombok.Data;

import java.io.Serializable;

/**
 * MQ重试消息DTO
 */
@Data
public class MqRetryMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息对象
     */
    private MqSendMessageDO message;

    /**
     * 重试次数
     * 0=首次重试（延迟5秒）
     * 1=第二次重试（延迟1分钟）
     */
    private int retryCount;
}
