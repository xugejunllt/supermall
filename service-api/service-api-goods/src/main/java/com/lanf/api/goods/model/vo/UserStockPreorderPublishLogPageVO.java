package com.lanf.api.goods.model.vo;


import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import com.lanf.constant.model.enums.storage.PublishStatusEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存预售发布日志分页VO
 */
@Data
public class UserStockPreorderPublishLogPageVO implements Serializable {

    /** ID */
    private Long id;

    /** 流水号 */
    private String flowNo;

    /** 库存ID */
    private Long stockId;

    /** SKU编码 */
    private String skuCode;

    /** 变更数量 */
    private Integer changeQuantity;

    /** 事件类型 */
    private StockPreorderEventTypeEnum eventType;

    /** 发布平台 */
    private PublishPlatformEnum publishPlatform;

    /** 仓库ID */
    private Long warehouseId;

    /** 商家ID */
    private Long tenantId;

    /** 状态 */
    private PublishStatusEnum status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
    /** 仓库名称 */
    private String warehouseName;
}
