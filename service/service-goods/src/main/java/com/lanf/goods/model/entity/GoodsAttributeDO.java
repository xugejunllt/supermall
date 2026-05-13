package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 商品属性
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-06
 */
@Data
@TableName("goods_attribute")
public class GoodsAttributeDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /** 属性 */
    private String attribute;

    /** 属性值 多个 用;隔开 */
    private String attributeValue;

    private Integer sort;
    
    private Long tenantId;

}
