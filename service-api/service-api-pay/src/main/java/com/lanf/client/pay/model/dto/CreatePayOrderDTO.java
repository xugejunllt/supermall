package com.lanf.client.pay.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Data
public class CreatePayOrderDTO implements Serializable {

    private Long mainOrderId;

    private Long shopId;
    //来源 0:订单支付 1:钱包充值
    private Integer source;
    //业务订单id
    private Long bizOrderId;

    private Long userId;
    //商家id
    private Long businessId;
    //优惠券id
    private Long couponId;

    private BigDecimal orderMoney;

}
