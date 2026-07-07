package com.lanf.api.storage.mq.message;

import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class RecycleStockMessage extends BaseMessage {


    private String flowNo;
    private Long tenantId;
    /**
     * sku编码
     */
    private String skuCode;

    private Integer changeQuantity;

    private Long warehouseId;
    /** 仓库名称 */
    private String warehouseName;
    private StockPreorderEventTypeEnum eventType;

    private PublishPlatformEnum publishPlatform;

}
