package com.lanf.order.model.dto;

import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.welfare.model.bo.DiscountInfoBO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO implements Serializable {


    private Long orderId;
    /**
     * 店铺id
     */
    private Long shopId;

    private String shopName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 订单金额
     */
    private BigDecimal totalMoney;

    /**
     * 实付金额
     */
    private BigDecimal actualPayMoney;
    private Long tenantId;
    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;
    /**
     * 收货地址
     */
    private AddressListVO addressListVO;
    /**
     * 优惠信息
     */
    List<DiscountInfoBO> discountInfoBOS;

    private List<OrderItemDTO> orderItems;
}
