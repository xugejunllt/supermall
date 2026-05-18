package com.lanf.order.model.bo;

import com.lanf.api.user.model.vo.AddressListVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderInitParamsBO implements Serializable {


    private String bizKeyPrx;

    private Long orderId;

    private Long userId;

    /**
     *  订单编号
     */
    private String orderNumber;

    private AddressListVO addressListVO;

}
