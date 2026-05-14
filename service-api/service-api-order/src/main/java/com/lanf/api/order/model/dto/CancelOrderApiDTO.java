package com.lanf.api.order.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class CancelOrderApiDTO implements Serializable {

    @NotBlank(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "业务Key后缀不能为空")
    private String bizKeySuffix;
}
