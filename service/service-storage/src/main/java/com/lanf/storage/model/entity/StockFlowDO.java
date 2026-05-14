package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.storage.model.enums.StockFlowTypeEnum;
import lombok.Data;

/**
 * <p>
 * 库存流水
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("stock_flow")
public class StockFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /** 唯一流水号 */
    private String flowNo;

    /** 库存id */
    private Long stockId;

    private StockFlowTypeEnum flowType;

    /** 商品sku编码 */
    private String skuCode;

    /** 关联业务单id */
    private Long bizOrderId;

    private Long orderId;

    /** 变更前数量 */
    private Integer beforeQuantity;

    /** 变动数量 */
    private Integer changeQuantity;

    /** 变更后数量 */
    private Integer afterQuantity;

    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    private Long tenantId;

    private String createDate;


}
