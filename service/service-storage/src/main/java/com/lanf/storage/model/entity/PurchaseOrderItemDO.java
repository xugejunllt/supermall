package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("purchase_order_item")
public class PurchaseOrderItemDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "采购单id")
    private Long purchaseOrderId;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "sku编码,库存最小单位")
    private String skuCode;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "销售单价")
    private BigDecimal salesUnitPrice;

    //total_money
    @ApiModelProperty(value = "总金额")
    private BigDecimal totalMoney;


    @ApiModelProperty(value = "备注")
    private String remarks;


}
