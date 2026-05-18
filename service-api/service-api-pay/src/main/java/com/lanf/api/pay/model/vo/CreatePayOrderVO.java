package com.lanf.api.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatePayOrderVO implements Serializable {

    //所有订单总金额
    private BigDecimal totalMoney;

    private List<CreatePayOrderItemVO> createPayOrderItemVOList;




}
