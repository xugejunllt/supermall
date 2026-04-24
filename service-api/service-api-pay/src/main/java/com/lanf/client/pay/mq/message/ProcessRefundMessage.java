package com.lanf.client.pay.mq.message;

import com.lanf.client.pay.model.enums.RefundEventTypeEnum;
import lombok.Data;

import java.io.Serializable;
@Data
public class ProcessRefundMessage implements Serializable {

    private String outRequestNo;

    private String  outTradeNo;

    private Integer payType;
    /**
     * 业务单id
     */
    private Long bizOrderId;

    private RefundEventTypeEnum refundEventTypeEnum;
}
