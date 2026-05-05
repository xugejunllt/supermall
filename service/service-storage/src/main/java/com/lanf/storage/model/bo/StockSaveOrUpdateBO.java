package com.lanf.storage.model.bo;

import com.lanf.storage.model.entity.StockDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class StockSaveOrUpdateBO implements Serializable {

   private List<StockDO> stockSave;

   private  List<StockUpdateBO> stockUpdate;

   private Map<String,Long> stockDOIdMap;
   private Map<String, Integer> currentTotalStockMap;
   public StockSaveOrUpdateBO(List<StockDO> stockSave,
                              List<StockUpdateBO> stockUpdate, Map<String, Long> stockDOIdMap
    , Map<String, Integer> currentTotalStockMap) {
      this.stockSave = stockSave;
      this.stockUpdate = stockUpdate;
      this.stockDOIdMap = stockDOIdMap;
      this.currentTotalStockMap = currentTotalStockMap;
   }
}
