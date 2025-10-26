package com.lanf.goods.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckAndQueryGoodsDTO implements Serializable {

    private Long skuId;;

    private Integer quantity;


}
