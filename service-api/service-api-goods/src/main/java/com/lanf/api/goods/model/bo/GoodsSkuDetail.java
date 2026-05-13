package com.lanf.api.goods.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SKU详情VO
 */
@Data
public class GoodsSkuDetail implements Serializable {

    /** skuCode */
    private String skuCode;

    /** sku名称 */
    private String attributeDetail;

    /** sku图片 */
    private String skuPictureAddress;

    /** 价格 */
    private BigDecimal price;

    /** 成本价格 */
    private BigDecimal costPrice;

    private List<StockDetail> stockDetailList;
}
