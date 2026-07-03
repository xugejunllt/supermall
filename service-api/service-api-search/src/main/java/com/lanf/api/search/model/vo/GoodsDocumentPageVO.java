package com.lanf.api.search.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品文档分页查询返回VO
 *
 * @author lanf
 */
@Data
public class GoodsDocumentPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品主图
     */
    private String mainImage;

    /**
     * 一级分类ID
     */
    private Long firstLevelCategoryId;

    /**
     * 一级分类名称
     */
    private String firstLevelCategoryName;

    /**
     * 二级分类ID
     */
    private Long secondaryLevelCategoryId;

    /**
     * 二级分类名称
     */
    private String secondaryLevelCategoryName;

    /**
     * 三级分类ID
     */
    private Long threeLevelCategoryId;

    /**
     * 三级分类名称
     */
    private String threeLevelCategoryName;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 上下架状态
     */
    private Integer upDownStatus;

    /**
     * 商品销量
     */
    private Long sales;

    /**
     * skuId
     */
    private Long skuId;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku价格
     */
    private Double price;

    /**
     * 扩展标签
     */
    private List<String> extendedTags;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

}
