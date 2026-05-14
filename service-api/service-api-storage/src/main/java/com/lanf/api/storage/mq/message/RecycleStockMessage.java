package com.lanf.api.storage.mq.message;

import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class RecycleStockMessage implements Serializable {


    private String flowNo;

    /**
     * sku编码
     */
    private String skuCode;

    private Integer changeQuantity;

    private Long warehouseId;

    private StockPreorderEventTypeEnum eventType;

    private PublishPlatformEnum publishPlatform;

}
