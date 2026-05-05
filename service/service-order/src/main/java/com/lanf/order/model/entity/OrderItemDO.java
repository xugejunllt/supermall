package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 订单商品项目
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Data
@TableName("order_item")
public class OrderItemDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    private String goodsTitle;

    @ApiModelProperty(value = "skuId")
    private Long skuId;

    private String skuCode;

    @ApiModelProperty(value = "sku名称")
    private String skuName;

    private String skuPictureAddress;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "单价")
    private BigDecimal unitPrice;
    //商品版本
    private Long goodsVersion;
    //sku 版本
    private Long skuVersion;

    private Long warehouseId;


}
