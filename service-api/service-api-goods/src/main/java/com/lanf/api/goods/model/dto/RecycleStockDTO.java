package com.lanf.api.goods.model.dto;

import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import lombok.Data;

import javax.validation.constraints.Min;
import java.io.Serializable;

@Data
public class RecycleStockDTO implements Serializable {


    /**
     * 库存id
     */
    private Long stockId;

    /**
     * sku编码
     */
    private String skuCode;

    @Min(value = 1, message = "数量不能小于1")
    private Integer changeQuantity;

    private PublishPlatformEnum publishPlatform;

    private Long warehouseId;

    private Long goodsId;

}
