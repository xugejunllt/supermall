package com.lanf.pay.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class CreatePayOrderGoodsItemDTO implements Serializable {

    //skuId
   // private Long id;

    private BigDecimal price;
    //数量
    private Integer quantity;



}
