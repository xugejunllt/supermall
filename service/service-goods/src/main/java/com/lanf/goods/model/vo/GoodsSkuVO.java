package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GoodsSkuVO implements Serializable {

    private Long id;

    private String skuCode;

    private String skuPictureAddress;

    private BigDecimal price;





}
