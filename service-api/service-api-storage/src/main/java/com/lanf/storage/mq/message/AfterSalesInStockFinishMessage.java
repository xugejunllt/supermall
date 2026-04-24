package com.lanf.storage.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class AfterSalesInStockFinishMessage implements Serializable {

    /**
     * 售后单id
     *
     */
    private Long afterSalesOrderId;

}
