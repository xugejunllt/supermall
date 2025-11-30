package com.lanf.goods.model.bo;

import com.lanf.goods.model.entity.StockDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StockSaveOrUpdateBO implements Serializable {

   private List<StockDO> stockSave;

   private  List<StockDO> stockUpdate;

   public StockSaveOrUpdateBO(List<StockDO> stockSave, List<StockDO> stockUpdate) {
      this.stockSave = stockSave;
      this.stockUpdate = stockUpdate;
   }
}
