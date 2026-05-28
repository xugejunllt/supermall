package com.lanf.api.order.model.dto;

import com.lanf.api.order.model.bo.AddressJson;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DeliveryDTO implements Serializable {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    //快递公司id
    @NotNull(message = "快递公司ID不能为空")
    private Long expressId;

    /**
     * 物流单号
     */
    @NotNull(message = "物流单号不能为空")
    private String trackingNumber;

    @NotNull(message = "发货地址不能为空")
    private AddressJson fromAddressJson;

}
