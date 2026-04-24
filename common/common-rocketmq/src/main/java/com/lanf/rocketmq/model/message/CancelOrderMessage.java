package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelOrderMessage implements Serializable {

    private String outRequestNo;

    private String outTradeNo;

    private Integer payType;

    private Integer cancelSource;

}
