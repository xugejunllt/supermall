package com.lanf.pay.model.bo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransferQueryResultBO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "支付宝转账单据号")
    private String orderId;

    @ApiModelProperty(value = "商户转账唯一订单号")
    private String outBizNo;

    @ApiModelProperty(value = "转账金额")
    private BigDecimal transAmount;

    @ApiModelProperty(value = "转账备注")
    private String remark;

    @ApiModelProperty(value = "转账完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "查询成功标识")
    private Boolean result;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;


}
