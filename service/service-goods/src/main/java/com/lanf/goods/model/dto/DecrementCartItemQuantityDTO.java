package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
@Data
public class DecrementCartItemQuantityDTO implements Serializable {

    @NotNull(message = "购物车id不能为空")
    private Long cartId;


    private Integer decrementQuantity;
}
