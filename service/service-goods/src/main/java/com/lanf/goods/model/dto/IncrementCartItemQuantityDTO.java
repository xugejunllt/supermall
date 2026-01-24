package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class IncrementCartItemQuantityDTO implements Serializable {

    @NotNull(message = "购物车id不能为空")
    private Long cartId;

    @Min(value = 1, message = "商品数量不能小于1")
    @NotNull(message = "商品数量不能为空")
    private Integer incrementQuantity;
}
