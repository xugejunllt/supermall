package com.lanf.api.storage.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SalesOutStockOrderFinishMessage implements Serializable {

    private Long orderId;
}
