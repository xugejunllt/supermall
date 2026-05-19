package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class BathDeductStockDTO implements Serializable {

    @NotNull(message = "扣减库存参数不能为空")
    private List<DeductStockDTO> deductStockDTOList;

}
