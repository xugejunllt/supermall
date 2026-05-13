package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 对账库存流水VO
 */
@Data
public class ReconciliationStockFlowVO implements Serializable {


    private List<ReconciliationStockFlow> reconciliationStockFlowList;

}
