package com.lanf.search.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Data

@Document(indexName = "goods_index", createIndex = false)
public class GoodsDocument {
    // 字段名称常量定义
    public static final String GOODS_ID = "goodsId";
    public static final String GOODS_NAME = "goodsName";
    public static final String SUB_TITLE = "subTitle";
    public static final String SHOP_ID = "shopId";
    public static final String SHOP_NAME = "shopName";
    public static final String MAIN_IMAGE = "mainImage";
    public static final String FIRST_LEVEL_CATEGORY_ID = "firstLevelCategoryId";
    public static final String FIRST_LEVEL_CATEGORY_NAME = "firstLevelCategoryName";
    public static final String SECONDARY_LEVEL_CATEGORY_ID = "secondaryLevelCategoryId";
    public static final String SECONDARY_LEVEL_CATEGORY_NAME = "secondaryLevelCategoryName";
    public static final String THREE_LEVEL_CATEGORY_ID = "threeLevelCategoryId";
    public static final String THREE_LEVEL_CATEGORY_NAME = "threeLevelCategoryName";
    public static final String BRAND_ID = "brandId";
    public static final String BRAND_NAME = "brandName";
    public static final String UP_DOWN_STATUS = "upDownStatus";
    public static final String SALES = "sales";
    public static final String TENANT_ID = "tenantId";
    public static final String VERSION = "version";
    public static final String CREATE_TIME = "createTime";
    public static final String UPDATE_TIME = "updateTime";
    public static final String IS_DELETED = "isDeleted";
    public static final String SKU_ID = "skuId";
    public static final String SKU_NAME = "skuName";
    public static final String PRICE = "price";
    public static final String ATTRIBUTES = "attributes";
    public static final String PROMPT_WORD_LABEL = "promptWordLabel";
    public static final String EXTENDED_TAGS = "extendedTags";

    @Id
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
    //商品属性 用于搜索 集合中只能嵌套对象
    private List<Attribute> attributes;
    // 搜索提示词标签 扩展词/联想词
    private List<String> promptWordLabel;
    //扩展标签 用于展示
    private List<String> extendedTags;

    /**
     * 嵌套属性类
     */
    @Data

    public static class Attribute {

        private String attrName;
        private String attrValue;

        public Attribute() {
        }

        public Attribute(String attrName, String attrValue) {
            this.attrName = attrName;
            this.attrValue = attrValue;
        }
    }



}