package com.lanf.tcc.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * tcc操作记录 用于去重
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
@Data
@TableName("tcc_operation")
public class TccOperationDO extends BaseEntity {

private static final long serialVersionUID=1L;

    /**
     * 参与者事务id
     */
    private Long participantId;

    /**
     * 业务唯一key
     * 与mq消息里的key同理
     */
    private String bizKey;

    // 阶段 0: try  1:confirm, 2：cancel阶段
    private Integer status;

    //try阶段参数，用于下一阶段
    private String parameter;
    /**
     *
     * 0：正常  1：中断
     * 被中断 将不再被执行或重试
     *
     */
    private Integer interruptedFlag;
    /**
     * 中断时发生的异常
     */
    private String interruptedException;

    /**
     * 分片键 如果是分片表
     */
    private String shardingValue;

    private Long version;




}
