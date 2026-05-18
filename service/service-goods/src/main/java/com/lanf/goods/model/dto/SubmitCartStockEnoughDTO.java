package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class SubmitCartStockEnoughDTO implements Serializable {

    @NotNull(message = "库存信息不能为空")
    private List<StockEnoughDTO> stockEnoughDTOS;
}
