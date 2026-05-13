package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 基础商品
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Data
@TableName("goods")
public class GoodsDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /** 商品编码 */
    private String code;

    /** 店铺id */
    private Long shopId;
    
    /** 商品名称 */
    private String name;

    /** 副标题 */
    private String title;

    /** 图片地址 */
    private String pictureAddress;

    /** 商品3级分类 */
    private Long categoryId;

    /** 品牌 */
    private Long brandId;

    /** 上下架状态 0:上架 ,1:下架 */
    private Integer upDownStatus;
    
    /** 搜索提示词标签 */
    private String promptWordLabel;
    
    /** 扩展标签 用于展示 */
    private String extendedTags;
    
    /** sku详细 加速详细读 */
    private String skuAttributeDetail;
    
    /** 属性code -skuCde映射 加速详细读 */
    private String unitCodeSkuCode;

    @TableField( fill = FieldFill.INSERT)
    private Long  tenantId;

    private Long version;

}
