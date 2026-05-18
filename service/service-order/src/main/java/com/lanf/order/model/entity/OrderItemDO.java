package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
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


    /**
     * 订单id
     */
    private Long orderId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 商品id
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    private String goodsTitle;

    /**
     * skuId
     */
    private Long skuId;

    private String skuCode;

    /**
     * sku名称
     */
    private String skuName;

    private String skuPictureAddress;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;
    //商品版本
    private Long goodsVersion;
    //sku 版本
    private Long skuVersion;

    private Long warehouseId;
    private Long tenantId;

}
