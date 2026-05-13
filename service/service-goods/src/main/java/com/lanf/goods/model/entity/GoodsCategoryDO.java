package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 商品分类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Data
@TableName("goods_category")
public class GoodsCategoryDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /** 名称 */
    private String name;

    /** 上级分类id */
    private Long parentId;

    /** 排序坐标，越大越靠前 */
    private Integer sortIndex;

    private Integer level;

    private Long  tenantId;

}
