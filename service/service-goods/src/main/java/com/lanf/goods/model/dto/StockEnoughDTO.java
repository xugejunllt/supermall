package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class StockEnoughDTO {

    //skuCode
    @NotBlank(message = "skuCode不能为空")
    private String skuCode;
    //商品购买数量
    @NotNull(message = "商品购买数量不能为空")
    @Min(value = 1, message = "商品购买数量不能小于1")
    private Integer quantity;

}
