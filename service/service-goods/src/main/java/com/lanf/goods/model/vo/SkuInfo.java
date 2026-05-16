package com.lanf.goods.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class SkuInfo {
    private Long skuId;
    private String skuCode;
    private BigDecimal price;
    private String image;

    private Map<String, String> attributes;

}
