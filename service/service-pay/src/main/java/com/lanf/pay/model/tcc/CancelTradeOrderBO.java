package com.lanf.pay.model.tcc;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelTradeOrderBO implements Serializable {

    /**
     * 当前支付状态
     */
   private Integer currentPayStatus;
}
