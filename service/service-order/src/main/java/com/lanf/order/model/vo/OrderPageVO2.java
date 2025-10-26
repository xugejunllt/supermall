package com.lanf.order.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.bouncycastle.cms.PasswordRecipientId;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderPageVO2 implements Serializable {

    private Long id;

    //支付金额
    private BigDecimal payMoney;
    //下单时间
    private Date createTime;
    //支付方式
    private Integer payType;
    //订单状态
    private Integer status;
     //订单编号
    private String orderNumber;

    //收货人
    private String consignee;
    //收货人联系电话
    private String phone;
    //收货地址
    private String takeAddress;
}
