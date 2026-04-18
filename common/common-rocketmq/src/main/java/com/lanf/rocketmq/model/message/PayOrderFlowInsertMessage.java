package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付流水插入成功通知消息
 */
@Data
public class PayOrderFlowInsertMessage implements Serializable {

    private static final long serialVersionUID = 1L;

//    /**
//     * 支付类型 0支付宝 1微信 2银联
//     */
//    private Integer payType;

    private Long orderId;

    private Long mainOrderId;
    /**
     * 是否批量支付
     */
    private Boolean bathPay;


}
