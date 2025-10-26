package com.lanf.storage.model.bo;

import com.lanf.storage.model.entity.StockDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StockSaveOrUpdateBO implements Serializable {

   private List<StockDO> stockSave;

   private  List<StockUpdateBO> stockUpdate;

   public StockSaveOrUpdateBO(List<StockDO> stockSave, List<StockUpdateBO> stockUpdate) {
      this.stockSave = stockSave;
      this.stockUpdate = stockUpdate;
   }
}
