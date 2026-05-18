package com.lanf.pay.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CreatePrepayOrderDTO implements Serializable {


    @NotBlank(message = "订单编号不能为空")
    private String orderNumber;

    /**
     * 支付类型 0支付宝 1微信 2银联 
     */
    @NotNull(message = "支付类型不能为空")
    private Integer payType;

}
