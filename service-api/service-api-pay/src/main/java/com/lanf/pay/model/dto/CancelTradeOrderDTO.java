package com.lanf.pay.model.dto;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class CancelTradeOrderDTO implements Serializable {

    @NotBlank(message = "业务key前缀不能为空")
    private String bizKeyPrx;

    @NonNull
    private Long orderId;
}
