package com.lanf.pay.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransferResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean transferSuccess;

    @ApiModelProperty(value = "转账订单号")
    private String outBizNo;

    @ApiModelProperty(value = "支付宝转账单据号")
    private String orderId;

    @ApiModelProperty(value = "转账状态：SUCCESS-成功, FAIL-失败, PROCESSING-处理中")
    private String status;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal transferAmount;

    @ApiModelProperty(value = "收款方支付宝账号")
    private String payeeAccount;

    @ApiModelProperty(value = "收款方姓名（脱敏）")
    private String payeeRealName;

    @ApiModelProperty(value = "转账完成时间")
    private Date finishTime;



}
