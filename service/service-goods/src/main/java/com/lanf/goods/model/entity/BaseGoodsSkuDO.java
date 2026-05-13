package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Data
@TableName("base_goods_sku")
public class BaseGoodsSkuDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private Long goodsId;

    /** skuCode */
    private String skuCode;

    /** sku 属性 */
    private String attributes;

    /** sku详细 */
    private String attributeDetail;

    /** 排序（影响展示顺序） */
    private Integer sort;
    
    private Long  tenantId;

}
