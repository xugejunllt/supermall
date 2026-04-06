package com.lanf.pay.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class BathCreatePrepayOrderDTO implements Serializable {

    @NotNull(message = "主订单id不能为空")
    private Long mainOrderId;

    @NotNull(message = "支付类型不能为空")
    @ApiModelProperty(value = "支付类型 0支付宝 1微信 2银联 ")
    private Integer payType;


}
