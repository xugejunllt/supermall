package com.lanf.api.goods.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BathDeductStockDTO implements Serializable {

    private List<DeductStockDTO> deductStockDTOList;

}
