package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 销售出库单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Data
@TableName("sales_out_stock_order")
public class SalesOutStockOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "单据编码")
    private String code;

    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "预计出库数量")
    private Integer expectQuantity;

    @ApiModelProperty(value = "实际出库数量")
    private Integer actualQuantity;

    @ApiModelProperty(value = "出库状态0:待出库, 2:已出库 ")
    private Integer storageStatus;

    @ApiModelProperty(value = "物流公司")
    private String expressCompany;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;

    @ApiModelProperty(value = "备注")
    private String remarks;




}
