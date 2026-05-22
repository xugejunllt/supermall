package com.lanf.aftersales.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SalesInStockOrderAddMessage implements Serializable {


    /**
     * 售后单id
     */
    private Long afterSalesOrderId;

    private Long tenantId;

    private List<SalesInStockOrderItemAdd> salesInStockOrderItemAddDTOList;




}
