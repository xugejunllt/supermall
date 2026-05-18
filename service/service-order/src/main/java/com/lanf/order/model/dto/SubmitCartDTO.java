package com.lanf.order.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class SubmitCartDTO implements Serializable {

    //订单编号
    @NotBlank(message = "订单编号不能为空")
    private  String mainOrderNumber;

    @NotNull(message = "收货地址id不能为空")
    private Long addressId;



    //购物车id
    @NotNull(message = "购物车信息不能为空")
    private List<CartInfoDTO> cartInfoList;

}
