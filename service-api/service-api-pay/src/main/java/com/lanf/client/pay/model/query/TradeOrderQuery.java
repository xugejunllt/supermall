package com.lanf.client.pay.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class TradeOrderQuery implements Serializable {

    private Long orderId;

    private Integer source;


}
