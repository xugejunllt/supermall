package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GoodsItemBO implements Serializable {

    private Long skuId;

    private Long cartId;

    private String skuName;

    private String goodsName;
    //数量
    private Integer quantity;
    //价格
    private BigDecimal price;

}
