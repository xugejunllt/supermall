package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ReconciliationStockFlowVO implements Serializable {


    private List<ReconciliationStockFlow> reconciliationStockFlowList;


}
