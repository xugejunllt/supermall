package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.ReconciliationJobStatusEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 对账任务执行记录表
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@Data
@TableName("reconciliation_job_log")
public class ReconciliationJobLogDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "账单id")
    private String batchId;

    @ApiModelProperty(value = "任务类型，0.交易单长款扫描，1：交易单短款扫描， 2.退款单长款扫描，3：退款单短款扫描， 4.转账单长款扫描 ，5：转账单短款扫描")
    private ReconciliationJobTypeEnum jobType;

    @ApiModelProperty(value = "任务状态: 0--执行中, 1-扫描已完成 , 2-对账完成  ")
    private ReconciliationJobStatusEnum status;




}
