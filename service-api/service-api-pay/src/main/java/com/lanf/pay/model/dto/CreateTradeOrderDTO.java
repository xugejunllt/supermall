package com.lanf.pay.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CreateTradeOrderDTO implements Serializable {

    @NotBlank(message = "业务key前缀不能为空")
    private String bizKeyPrx;
    // 用户id
    @NotNull( message = "用户id不能为空")
    private Long userId;
    //订单id
    @NotNull( message = "订单id不能为空")
    private Long orderId;

    @NotNull(message = "交易金额不能为空")
    private BigDecimal tradeMoney;

    @NotNull(message = "支付类型不能为空")
    @ApiModelProperty(value = "支付类型 0支付宝 1微信 2银联 ")
    private Integer payType;

}
