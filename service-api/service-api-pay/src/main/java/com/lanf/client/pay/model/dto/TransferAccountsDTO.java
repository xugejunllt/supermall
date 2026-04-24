package com.lanf.client.pay.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransferAccountsDTO implements Serializable {



    private Long userId;
    //店铺id
    private Long shopId;
    @ApiModelProperty(value = "业务来源,0:结算转账给商家,1:用户取消订单退款，转账给用户")
    private Integer source;
    @ApiModelProperty(value = "收款用户类型0:商家,1平台用户")
    private Integer toUserType;
    @ApiModelProperty(value = "商家侧唯一订单号")
    private String outBizNo;
    @ApiModelProperty(value = "收款账户类型 0:支付宝 1:银行卡")
    private Integer toAccountType;

    @ApiModelProperty(value = "收款账号")
    private String incomeAccount;

    @ApiModelProperty(value = "支付类型 0:支付宝,1:微信")
    private Integer payType;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal transAmount;
    //收款人身份证号
    private  String certNo;
    //收款人姓名
    private String name;
    private String orderTitle;

}
