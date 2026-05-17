package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class IncrementCartItemQuantityDTO implements Serializable {

    @NotNull(message = "购物车id不能为空")
    private Long cartId;

    @Min(value = 1, message = "商品数量不能小于1")
    @NotNull(message = "商品数量不能为空")
    private Integer incrementQuantity;

    /**
     * 如果没有默认地址信息，则使用定位获取
     */
    private String areaCode;

    /**
     * 纬度
     */
    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;
}
