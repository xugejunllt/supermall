package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CartGoodsVO implements Serializable {

    private Long id;
    //商品id
    private Long goodsId;
    //
    private String name;
    //sku图片
    private String skuPictureAddress;
    //价格
    private BigDecimal price;
    //数量
    private Integer quantity;

    private String skuName;

    //当前sku属性值
    private String selectedAttr ="红色的衣服";
    private Boolean checked = false;

    //店铺id
    private Long shopId;
    //店铺名称
    private String shopName;

    private Boolean last = false;
}
