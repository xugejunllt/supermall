package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
public class StockQueryBySkuDTO implements Serializable {

    @NotEmpty(message = "SKU编码列表不能为空")
    private List<String> skuCodes;

    private String areaCode;
}
