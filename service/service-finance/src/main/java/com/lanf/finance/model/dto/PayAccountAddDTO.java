package com.lanf.finance.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PayAccountAddDTO implements Serializable {




    @ApiModelProperty(value = "账户类型 0:支付宝")
    private Integer accountType;

    @ApiModelProperty(value = "账户")
    private String account;

    //初期余额
    private BigDecimal startRemainMoney;
    //用途 0:订单账号
    private Integer useTo;
}
