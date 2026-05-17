package com.lanf.api.storage.mq.message;

import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PublishStockMessage implements Serializable {


    private String flowNo;

    private String skuCode;

    private Integer changeQuantity;

    private Long warehouseId;

    private Long tenantId;

    private String warehouseName;

    private Long goodsId;

    private String areaCode;

    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;
    private StockPreorderEventTypeEnum eventType;

    private PublishPlatformEnum publishPlatform;

}
