package com.lanf.order.model.bo;

import com.lanf.constant.model.enums.order.OrderStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddOrderStatusTrace implements Serializable {


    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    private OrderStatusEnum fromStatus;

    @ApiModelProperty(value = "变更后状态（同状态枚举）")
    private OrderStatusEnum toStatus;


    @ApiModelProperty(value = "备注信息，例如“7天未评价自动完成”")
    private String remark;

}
