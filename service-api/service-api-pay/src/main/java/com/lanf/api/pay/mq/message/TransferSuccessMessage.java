package com.lanf.api.pay.mq.message;

import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.constant.mq.base.BaseMessage;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferSuccessMessage extends BaseMessage {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联事件对应的业务单id")
    private Long bizOrderId;

    @ApiModelProperty(value = "事件类型 0：订单结算给商家，1：用户钱包提现")
    private TransferEventTypeEnum eventType;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal transAmount;

    /**
     * 转账结果
     */
    private Boolean result;


}
