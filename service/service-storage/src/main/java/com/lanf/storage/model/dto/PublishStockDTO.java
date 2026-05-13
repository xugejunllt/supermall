package com.lanf.storage.model.dto;

import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PublishStockDTO implements Serializable {

    @ApiModelProperty(value = "库存id")
    private Long stockId;

    @ApiModelProperty(value = "sku编码")
    private String skuCode;

    private Integer changeQuantity;

    private PublishPlatformEnum publishPlatform;

    private Long warehouseId;

    private Long merchantId;
}
