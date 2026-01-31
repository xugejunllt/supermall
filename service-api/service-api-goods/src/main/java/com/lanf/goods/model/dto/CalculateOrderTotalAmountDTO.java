package com.lanf.goods.model.dto;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CalculateOrderTotalAmountDTO {


    @NotNull(message = "商品ID不能为空")
    private Long skuId;

    //购买数量
    @NotNull(message = "购买数量不能为空")
    private Integer quantity;


}
