package com.lanf.pay.model.bo;

import com.lanf.constant.model.enums.pay.RefundEventTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ProcessRefund implements Serializable {

    private String outRequestNo;

    private String  outTradeNo;

    private Integer payType;
    /**
     * 业务单id
     */
    private Long bizOrderId;

    private RefundEventTypeEnum refundEventTypeEnum;


}
