package com.lanf.order.model.dto;

import com.lanf.constant.model.enums.order.AfterSalesTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class AddAfterSalesOrderDTO implements Serializable {

    /**
     * 订单id
     */
    @NotNull(message = "订单id不能为空")
    private Long orderId;

    @NotNull(message = "售后类型不能为空")
    private AfterSalesTypeEnum afterSalesType;

    /**
     * 退款原因
     */
    @NotNull(message = "退款原因不能为空")
    private String returnReason;





}
