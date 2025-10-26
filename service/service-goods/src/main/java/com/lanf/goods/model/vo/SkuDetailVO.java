package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SkuDetailVO implements Serializable {

    private Long id;

    private Long shopId;

    private String shopName;

    private String skuPictureAddress;

    private String goodsName;

    private String skuName;

    private BigDecimal price;


}

