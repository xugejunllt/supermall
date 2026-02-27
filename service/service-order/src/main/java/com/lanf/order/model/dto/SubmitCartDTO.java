package com.lanf.order.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
public class SubmitCartDTO implements Serializable {

    //订单编号
    private  String mainOrderNumber;

    //支付类型 0支付宝 1微信 2银联
    private Integer payType;

    //收货地址
    private TakeAddressDTO takeAddress;

    //购物车id
    @NotEmpty(message = "购物车id不能为空")
    private List<Long> cartIds;

}
