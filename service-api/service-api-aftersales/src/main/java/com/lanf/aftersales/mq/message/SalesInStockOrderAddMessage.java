package com.lanf.aftersales.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class SalesInStockOrderAddMessage extends BaseMessage {


    /**
     * 售后单id
     */
    private Long afterSalesOrderId;

    private Long tenantId;

    private List<SalesInStockOrderItemAdd> salesInStockOrderItemAddDTOList;




}
