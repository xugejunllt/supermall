package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU分页查询返回VO
 *
 * @author lanf
 */
@Data
public class GoodsSkuPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * sku属性
     */
    private String attributes;

    /**
     * sku详细
     */
    private String attributeDetail;

    /**
     * skuCode
     */
    private String skuCode;

    /**
     * sku图片
     */
    private String skuPictureAddress;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 0:没有选中 ,1:默认选中
     */
    private Integer defaultSelect;

    /**
     * 排序
     */
    private Integer sort;

}
