package com.lanf.rocketmq.model.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class RefundDTO  {


    private Long orderId;
    @ApiModelProperty(value = "业务来源,0:结算转账给商家,1:用户取消订单退款，转账给用户,2:售后退货退款")
    private Integer source;
    @ApiModelProperty(value = "收款用户类型0:商家,1平台用户")
    private Integer toUserType;

    //转账
    private Boolean transAccount;

}
