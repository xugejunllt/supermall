package com.lanf.api.order.mq.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 出入库单商品明细
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Data
public class InOutStockOrderItem implements Serializable {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "sku编码,库存最小单位")
    private String skuCode;

    @ApiModelProperty(value = "总数量")
    private Integer totalQuantity;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "仓库id")
    private Long warehouseId;

    private Long tenantId;

}
