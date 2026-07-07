package com.lanf.storage.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class ShortStockReconciliationMessage extends BaseMessage {

     private String bathId;

     private List<ShortStockReconciliation> reconciliationList;
}
