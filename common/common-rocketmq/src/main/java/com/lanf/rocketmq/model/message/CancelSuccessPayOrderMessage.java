package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelSuccessPayOrderMessage implements Serializable {

    private String  outTradeNo;

}
