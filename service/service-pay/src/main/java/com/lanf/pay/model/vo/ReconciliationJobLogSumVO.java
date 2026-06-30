package com.lanf.pay.model.vo;

import com.lanf.pay.model.enums.ReconciliationJobStatusEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ReconciliationJobLogSumVO implements Serializable {


    private Long id;
    /**
     * 账单id
     */
    private String batchId;


    private ReconciliationJobTypeEnum jobType;

    /**
     * 任务状态: 0--执行中, 1-扫描已完成 , 2-对账完成
     */
    private ReconciliationJobStatusEnum status;

    private Date updateTime;
    /**
     * 流水数量
     */
    private Integer flowCount;
    /**
     * 已完成对账的数量
     */
    private Integer diffMarker;



}
