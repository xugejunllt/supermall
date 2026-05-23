package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 秒杀库存预占DTO
 */
@Data
public class SeckillStockPreoccupationDTO implements Serializable {

    @NotBlank(message = "业务key前缀不能为空")
    private String bizKeyPrx;

    private String skuCode;

    private Long warehouseId;
    private Long goodsId;
    /**
     * 预占数量
     */
    private Integer preQuantity;
}
