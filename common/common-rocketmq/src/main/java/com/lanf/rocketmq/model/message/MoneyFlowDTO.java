package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class MoneyFlowDTO implements Serializable {



    private Long orderId;

    private Integer source;

}
