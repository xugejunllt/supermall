package com.lanf.api.goods.model.vo;

import com.lanf.api.goods.model.bo.Attributes;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 基础商品SKU按编码查询VO
 */
@Data
public class BaseGoodsSkuByCodeQueryVO implements Serializable {


    private Long baseGoodsSkuId;

    private Long goodsId;

    /** skuCode */
    private String skuCode;

    /** 排序（影响展示顺序） */
    private Integer sort;

    /** sku详细 */
    private String attributeDetail;
    /** sku 属性 */
    private List<Attributes> attributes;





}
