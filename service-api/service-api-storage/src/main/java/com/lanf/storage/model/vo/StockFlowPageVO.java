package com.lanf.storage.model.vo;

import com.lanf.constant.model.enums.storage.StockFlowTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class StockFlowPageVO implements Serializable {

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

    private Long id;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;

}
