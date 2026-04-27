package com.lanf.pay.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class BalanceOrderDTO implements Serializable {


    @NotBlank(message = "订单编号不能为空")
    private String orderNumber;


}
