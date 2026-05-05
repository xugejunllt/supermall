package com.lanf.order.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AddSalesOutStockOrderMessage implements Serializable {


    private Long orderId;

    private List<InOutStockOrderItem> items;

}
