package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Data
@TableName("goods_sku")
public class GoodsSkuDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long goodsId;

    /** sku 属性 */
    private String attributes;

    /** sku详细 */
    private String attributeDetail;

    /** skuCode */
    private String skuCode;

    /** sku图片 */
    private String skuPictureAddress;

    /** 价格 */
    private BigDecimal price;

    /** 成本价格 */
    private BigDecimal costPrice;
    
    /** 0:没有选中 ,1:默认选中 */
    private Integer defaultSelect;

    private Integer sort;

    private Long version;
    private Long  tenantId;
}
