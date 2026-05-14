package com.lanf.api.goods.model.vo;


import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存流水分页VO
 */
@Data
public class UserStockFlowPageVO implements Serializable {

    /** ID */
    private Long id;

    /** 流水号 */
    private String flowNo;

    /** 库存ID */
    private Long userStockId;

    /** SKU编码 */
    private String skuCode;

    /** 仓库ID */
    private Long warehouseId;

    /** 订单ID */
    private Long orderId;

    /** 事件类型 */
    private UserStockFlowEventTypeEnum eventType;

    /** 变更前数量 */
    private Integer beforeQuantity;

    /** 变更数量 */
    private Integer changeQuantity;

    /** 变更后数量 */
    private Integer afterQuantity;

    /** 创建时间 */
    private Date createTime;

    private String warehouseName;

}
