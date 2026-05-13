package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 基础商品SKU按编码查询VO
 */
@Data
public class BaseGoodsSkuByCodeQueryVO implements Serializable {

    private Long goodsId;

    /** skuCode */
    private String skuCode;
    
    /** 可使用库存 */
    private Integer usableStock;
    
    /** sku名称 */
    private String skuName;
    
    /** sku名称 */
    private String attribute;

    /** sku描述 */
    private String attributeDesc;

    /** sku图片 */
    private String skuPictureAddress;

    private List<SkuNameVO> skuNameVOList;

    /** 排序（影响展示顺序） */
    private Integer sort;
}
