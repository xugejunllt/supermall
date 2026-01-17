package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CalculateOrderAmountDTO implements Serializable {

    @NotNull(message = "店铺ID不能为空")
    private Long shopId;
    @NotNull(message = "商品ID不能为空")
    private Long skuId;
    //购买数量
    @NotNull(message = "购买数量不能为空")
    private Integer quantity;
}
