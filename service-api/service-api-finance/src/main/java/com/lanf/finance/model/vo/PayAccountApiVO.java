package com.lanf.finance.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PayAccountApiVO implements Serializable {


    @ApiModelProperty(value = "商家id")
    private Long businessId;

    @ApiModelProperty(value = "用户类型 0:平台 1:商家")
    private Integer userType;

    @ApiModelProperty(value = "账户类型 0:支付宝")
    private Integer accountType;

    @ApiModelProperty(value = "账户")
    private String account;

    @ApiModelProperty(value = "支付秘钥")
    private String paySecretKey;
}
