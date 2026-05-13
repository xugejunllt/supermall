package com.lanf.storage.mq.message;

import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class RecycleStockMessage implements Serializable {


    private String flowNo;

    @ApiModelProperty(value = "sku编码")
    private String skuCode;

    private Integer changeQuantity;

    private Long warehouseId;

    private StockPreorderEventTypeEnum eventType;

    private PublishPlatformEnum publishPlatform;

}
