package com.lanf.order.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class CalculateOrderAmountDTO implements Serializable {


    @NotNull(message = "商品ID不能为空")
    private Long skuId;
    //购买数量
    @NotNull(message = "购买数量不能为空")
    private Integer quantity;

    //优惠卷id
    private List<Long> couponIds;
}
