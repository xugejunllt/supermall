package com.lanf.pay.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueryRefundResultMessage implements Serializable {


    private String outTradeNo;

    private  String outRequestNo;


}
