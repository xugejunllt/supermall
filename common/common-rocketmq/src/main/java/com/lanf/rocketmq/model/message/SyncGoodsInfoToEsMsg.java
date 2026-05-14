package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SyncGoodsInfoToEsMsg implements Serializable {


    //商品id
    private Long goodsId;
    //商品名称
    private String goodsName;
    //副标题
    private String subTitle;
    //店铺id
    private Long shopId;
    //店铺名称
    private String shopName;
    //商品主图
    private String mainImage;
    //一级分类id
    private Long firstLevelCategoryId;
    //一级分类名称
    private String firstLevelCategoryName;
    //二级分类id
    private Long secondaryLevelCategoryId;
    //二级分类名称
    private String secondaryLevelCategoryName;
    //三级分类id
    private Long threeLevelCategoryId;
    //三级分类名称
    private String threeLevelCategoryName;
    //品牌id
    private Long brandId;
    //品牌名称
    private String brandName;
    //上下架状态
    private Integer upDownStatus;
    //商品销量
    private Long sales;
    //租户id
    private Long tenantId;
    //版本
    private Long version;
    //创建时间
    private Long createTime;
    //更新时间
    private Long updateTime;
    //删除标记
    private Integer isDeleted;
    //skuid
    private Long skuId;
    //sku名称
    private String skuName;
    // sku价格
    private Double price;
    //商品属性 用于搜索
    private List<Attribute> attributes;
    // 搜索提示词标签
    private List<String> promptWordLabel;
    //扩展标签 用于展示
    private List<String> extendedTags;

    /**
     * 嵌套属性类
     */
    @Data
    public static class Attribute {

        private Long skuId;

        private String attribute;

        private String attributeValue;
    }

}
