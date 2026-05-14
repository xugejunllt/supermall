package com.lanf.storage.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class InStockPurchaseInStockOrderDTO implements Serializable {

    //入库单id
    @NotNull( message = "入库单id不能为空")
    private Long purchaseInStockOrderId;

    @NotNull( message = "入库单商品明细不能为空")
    private List<InStockItemDTO> inStorageItemList;

}
