package com.lanf.welfare.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillPlaneMessage extends BaseMessage {


    private Long secKillItemId;

    private Long orderId;

    private Long shopId;

    private String shopName;
    //商家id
    private Long tenantId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "订单编号")
    private String orderNumber;

    @ApiModelProperty(value = "收货地址")
    private String takeAddress;

    /**
     * 售后有效期，如果多个商品不同售后期，那么取最大的
     */
    private Integer afterSaleDays;


    //订单项目信息
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
