package com.lanf.api.pay.model.dto;

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

    @NotEmpty
    private List<CreateMergeTradeOrderItemDTO> tradeOrderItemList;

}
