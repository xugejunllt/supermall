package com.lanf.storage.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LongStockReconciliationMessage implements Serializable {

     private String bathId;

     private List<LongStockReconciliation> reconciliationList;

}
