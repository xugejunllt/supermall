package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DeductStockDTO implements Serializable {

    @NotBlank(message = "业务key前缀不能为空")
    private String bizKeyPrx;

    @NotNull(message = "订单id不能为空")
    private Long orderId;

    @NotBlank(message = "skuCode不能为空")
    private String skuCode;

    //数量
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于1")
    private Integer quantity;

    @NotNull(message = "商品id不能为空")
    private Long goodsId;

    @NotNull(message = "仓库id不能为空")
    private Long warehouseId;

    @NotBlank(message = "订单编号不能为空")
    private String orderNumber;
}
