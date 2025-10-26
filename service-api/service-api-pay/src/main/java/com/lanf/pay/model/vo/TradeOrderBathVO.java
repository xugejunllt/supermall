package com.lanf.pay.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TradeOrderBathVO implements Serializable {

    private Long tradeOrderId;
    /**
     * 实际支付金额
     */
    private BigDecimal payMoney;
    @ApiModelProperty(value = "支付类型 0支付宝 1微信 2银联 ")
    private Integer payType;

    private Long orderId;
}
