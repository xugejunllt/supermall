package com.lanf.goods.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CalculateOrderAmountDTO implements Serializable {


    private Long skuId;
    //购买数量
    private Integer quantity;
}
