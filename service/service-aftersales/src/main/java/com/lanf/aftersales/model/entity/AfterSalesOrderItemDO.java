package com.lanf.aftersales.model.entity;

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
 * @since 2024-06-19
 */
@Data
@TableName("after_sales_order_item")
public class AfterSalesOrderItemDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "售后单id")
    private Long afterSalesOrderId;

    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    @ApiModelProperty(value = "商品标题")
    private String goodsName;

    @ApiModelProperty(value = "sku名称")
    private String skuName;

    private String skuCode;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "单价")
    private BigDecimal unitPrice;

    @ApiModelProperty(value = "总金额")
    private BigDecimal totalMoney;


}
