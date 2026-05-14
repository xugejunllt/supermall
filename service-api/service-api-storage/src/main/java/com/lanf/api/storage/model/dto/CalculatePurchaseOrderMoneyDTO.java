package com.lanf.api.storage.model.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CalculatePurchaseOrderMoneyDTO implements Serializable {


    @Max(value = 1000000,message = "采购运费超过最大值")
    @Min(value = 0,message = "采购运费小于最小值")
    /** 采购运费 */
    private BigDecimal freight;

    @Max(value = 1000000,message = "其他费用超过最大值")
    @Min(value = 0,message = "其他费用小于最小值")
    /** 其他费用 */
    private BigDecimal otherFreight;

    @NotEmpty
    private List<CalculatePurchaseOrderItemMoneyDTO> purchaseOrderItemMoneyList;
}
