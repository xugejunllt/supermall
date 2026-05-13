package com.lanf.user.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CalculationGrowthValue implements Serializable {




    /////
    /**
     * 等级变更明细需要的信息
     */
    private Long userId;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件code
     */
    private String eventCode;

    /**
     * 业务ID，如订单号
     */
    private String bizId;

    /**
     * 变动前等级
     */
    private Integer beforeLevel;

    /**
     * 变动后等级
     */
    private Integer afterLevel;

    /**
     * 使用的权益列表
     */
    private String levelPrivileges;

    /**
     * 变动的成长值
     */
    private Integer growthValue;

    /**
     * 变动前的总成长值
     */
    private Integer afterTotal;

    /**
     * 变动后的总成长值
     */
    private Integer currentTotal;


    /**
     * 等级主表需要的信息
     */
    private Integer level;

    /**
     * 当前等级ID
     */
    private Long levelId;


    /**
     * 是否升级了
     */
    private Boolean upgrade;

}
