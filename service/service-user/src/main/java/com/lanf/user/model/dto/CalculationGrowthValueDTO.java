package com.lanf.user.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CalculationGrowthValueDTO implements Serializable {

    //事件code
    private String eventCode;

    private Long userId;

    //业务ID，如订单号
    private String bizId;
}
