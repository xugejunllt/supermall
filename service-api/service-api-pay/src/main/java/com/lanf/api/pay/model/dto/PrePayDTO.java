package com.lanf.api.pay.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PrePayDTO implements Serializable {

    private Long mainOrderId;
    @ApiModelProperty(value = "支付类型 0支付宝 1微信 2银联")
    private Integer payType;

    private List<Long> orderId;
    //支付账户
    private String payAccount;

}
