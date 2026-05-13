package com.lanf.storage.mq.message;

import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PublishStockMessage implements Serializable {


    private String flowNo;

    @ApiModelProperty(value = "sku编码")
    private String skuCode;

    private Integer changeQuantity;

    private Long warehouseId;

    @ApiModelProperty(value = "商家id")
    private Long merchantId;

    private String goodsName;

    @ApiModelProperty(value = "单位")
    private String unit;

    private String warehouseName;

    private StockPreorderEventTypeEnum eventType;

    private PublishPlatformEnum publishPlatform;

}
