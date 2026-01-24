package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CartAddDTO implements Serializable {


    private Long userId;
    @NotBlank(message = "商品skuCode不能为空")
    private String skuCode;

    @NotNull(message = "商品数量不能为空")
    private Integer quantity;

}
