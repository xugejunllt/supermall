package com.lanf.rocketmq.model.message;

import com.lanf.messagemanager.client.model.base.BaseMqMessage;
import lombok.Data;

import java.util.List;

@Data
public class UserStockAddMsg extends BaseMqMessage {


    private Long  tenantId;

    private Long purchaseInStockOrderId;

    private List<UserStockMsg> userStockList;



}
