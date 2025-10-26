package com.lanf.order.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderPageVO implements Serializable {

    private Long orderId;

    private  Long shopId;

    private String shopName;

    private Integer status;
    //订单状态描述
    private String statusDesc;
    //付款金额描述
    private String  payMoneyDesc;


    //详细名称
    private List<OrderItemPageVO> itemPageVOList;
}
