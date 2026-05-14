package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 出入库单商品明细
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Data
@TableName("in_out_stock_order_item")
public class InOutStockOrderItemDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long inOutStockOrderId;

    /** 商品名称 */
    private String goodsName;

    /** sku编码,库存最小单位 */
    private String skuCode;

    /** 总数量 */
    private Integer totalQuantity;

    /** 剩余数量 */
    private Integer surplusQuantity;

    /** 单位 */
    private String unit;

    /** 仓库id */
    private Long warehouseId;

    private Long  tenantId;


}
