package com.lanf.pay.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CancelPaidOrderResultBO implements Serializable {


    private Boolean result ;

    @ApiModelProperty(value = "支付宝交易号")
    private String tradeNo;

    /**
     * 实际退款金额
     */
    private BigDecimal returnMoney;

    @ApiModelProperty(value = "用户的登录id【示例值】159****5620")
    private String buyerLogonId;

    private Date payFinishTime;

    /**
     * 发起退款时的退款金额
     */
    private BigDecimal preReturnMoney;
}
