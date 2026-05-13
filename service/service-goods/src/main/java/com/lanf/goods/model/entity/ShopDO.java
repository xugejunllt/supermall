package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 店铺信息
 * </p>
 *
 * @author jarven
 * @since 2025-11-30
 */
@Data
@TableName("shop")
public class ShopDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /**
     * 店铺名称
     */
    private String name;

    /**
     * 头像
     */
    private String headUrl;

    private Long  tenantId;




}
