package com.lanf.finance.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClearingOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 清算单ID
     */
    private Long clearingDetailId;

    private Long orderId;

}
