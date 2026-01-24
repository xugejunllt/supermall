package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Data
@TableName("cart")
public class CartDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long userId;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    private Long goodsId;

    private Long skuId;


    private String skuCode;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    //排序顺序，越小越靠前
    private Long sortOrder;

    private Long version;


}
