package com.lanf.storage.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShortStockReconciliationMessage implements Serializable {

     private String bathId;

     private List<ShortStockReconciliation> reconciliationList;
}
