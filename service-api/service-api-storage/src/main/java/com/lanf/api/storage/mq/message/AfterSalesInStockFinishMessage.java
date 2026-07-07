package com.lanf.api.storage.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class AfterSalesInStockFinishMessage extends BaseMessage {

    /**
     * 售后单id
     *
     */
    private Long afterSalesOrderId;

}
