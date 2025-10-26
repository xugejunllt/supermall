package com.lanf.order.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderDetailVO implements Serializable {

    private Long id;

    //支付金额
    private BigDecimal payMoney;
    //订单编号
    private String orderNumber;
    //支付方式
    private String payTypeName;
    //支付完成时间
    private Date payFinishTime;
    //下单时间
    private Date orderCreateTime;
    //收货地址
    private String takeAddress;

    private String orderStatusName;
    private  Long shopId;

    private String shopName;
    //详细名称
    private List<OrderItemPageVO> itemPageVOList;

}
