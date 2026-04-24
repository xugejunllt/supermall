package com.lanf.client.pay.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class CreateMergeTradeOrderDTO implements Serializable {

    @NotBlank(message = "订单编号不能为空")
    private  String mainOrderNumber;

    //批量交易单id
    @NotNull(message = "批量交易单id不能为空")
    private Long mainOrderId;
    // 用户id
    @NotNull( message = "用户id不能为空")
    private Long userId;

    @NotNull(message = "支付类型不能为空")
    @ApiModelProperty(value = "支付类型 0支付宝 1微信 2银联 ")
    private Integer payType;

    @NotEmpty
    private List<CreateMergeTradeOrderItemDTO> tradeOrderItemList;

}
