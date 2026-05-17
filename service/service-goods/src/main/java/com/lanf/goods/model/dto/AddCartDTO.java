package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AddCartDTO implements Serializable {


    private Long userId;
    @NotBlank(message = "商品skuCode不能为空")
    private String skuCode;

    @NotNull(message = "商品数量不能为空")
    private Integer quantity;

    private Long goodsId;

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
