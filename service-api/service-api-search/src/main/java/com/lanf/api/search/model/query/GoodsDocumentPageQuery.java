package com.lanf.api.search.model.query;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品文档分页查询条件
 *
 * @author lanf
 */
@Data
public class GoodsDocumentPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 租户ID（必填）
     */
    private Long tenantId;

    /**
     * 商品名称（模糊查询）
     */
    private String goodsName;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 一级分类ID
     */
    private Long firstLevelCategoryId;

    /**
     * 二级分类ID
     */
    private Long secondaryLevelCategoryId;

    /**
     * 三级分类ID
     */
    private Long threeLevelCategoryId;

    /**
     * 上下架状态
     */
    private Integer upDownStatus;

    /**
     * 页码
     */
    protected long page = 1;

    /**
     * 每页大小
     */
    protected long pageSize = 20;

}
