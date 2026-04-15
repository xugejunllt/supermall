package com.lanf.pay.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class CancelTradeOrderDTO implements Serializable {

    @NotBlank(message = "业务key前缀不能为空")
    private String bizKeySuffix;

    @NotBlank( message = "订单id不能为空")
    private Long orderId;
}
