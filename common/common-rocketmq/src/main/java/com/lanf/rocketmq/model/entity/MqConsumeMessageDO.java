package com.lanf.rocketmq.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * MQ本地事务消息消费表
 */
@Data
@TableName("mq_consume_message")
public class MqConsumeMessageDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识
     */
    private String messageId;


    private String retryStrategyBeanClass;


    /**
     * 消息状态 0:待消费 1:消费成功 2:消费失败
     */
    private Integer status;

    /**
     * 当前重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 参数类型JSON
     */
    private String paramTypes;

    /**
     * 参数值JSON
     */
    private String paramValues;

    /**
     * 错误信息
     */
    private String errorMsg;
}
