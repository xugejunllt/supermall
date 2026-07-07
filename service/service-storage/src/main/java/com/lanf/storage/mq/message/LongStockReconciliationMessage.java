package com.lanf.storage.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class LongStockReconciliationMessage extends BaseMessage {

     private String bathId;

     private List<LongStockReconciliation> reconciliationList;

}
