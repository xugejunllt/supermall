package com.lanf.api.order.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class AllowOutboundDTO implements Serializable {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
