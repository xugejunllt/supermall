package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 商品变更版本
 * </p>
 *
 * @author jarven
 * @since 2025-12-05
 */
@Data
@TableName("goods_history_version")
public class GoodsHistoryVersionDO extends BaseEntity {

private static final long serialVersionUID=1L;



    /** 商品编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 店铺id */
    private Long shopId;

    /** 副标题 */
    private String title;

    /** 图片地址，多个,用"，"隔开 */
    private String pictureAddress;

    /** 商品3级分类 */
    private Long categoryId;

    /** 品牌 */
    private Long brandId;

    /** 上下架状态 0:上架 ,1:下架 */
    private Integer upDownStatus;

    private Long tenantId;

    private Long version;

    /** 搜索提示词标签 */
    private String promptWordLabel;
    
    /** 扩展标签 用于展示 */
    private String extendedTags;

}
