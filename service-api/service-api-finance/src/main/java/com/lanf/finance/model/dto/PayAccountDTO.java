package com.lanf.finance.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PayAccountDTO implements Serializable {


    @ApiModelProperty(value = "商家id")
    private Long businessId;

    @ApiModelProperty(value = "账户类型 0:支付宝")
    private Integer accountType;

}
