package com.lanf.api.storage.model.dto;

import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class PublishStockDTO implements Serializable {

    /** 库存id */
    private Long stockId;

    /** sku编码 */
    private String skuCode;

    private Integer changeQuantity;

    private PublishPlatformEnum publishPlatform;

    private Long warehouseId;


}
