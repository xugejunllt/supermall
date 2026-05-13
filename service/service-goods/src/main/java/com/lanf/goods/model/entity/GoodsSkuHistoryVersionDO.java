package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * sku变更版本
 * </p>
 *
 * @author jarven
 * @since 2025-12-05
 */
@Data
@TableName("goods_sku_history_version")
public class GoodsSkuHistoryVersionDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long goodsId;

    /** skuCode */
    private String skuCode;

    /** sku名称json */
    private String skuNameJson;

    /** sku名称 */
    private String skuName;

    /** sku图片 */
    private String skuPictureAddress;

    /** 价格 */
    private BigDecimal price;

    /** 成本价格 */
    private BigDecimal costPrice;

    /** sk展示排序码 */
    private Integer sort;

    private Long version;

}
