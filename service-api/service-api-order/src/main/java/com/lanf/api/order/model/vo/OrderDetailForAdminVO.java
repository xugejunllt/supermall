package com.lanf.api.order.model.vo;

import com.lanf.api.order.model.bo.AddressJson;
import com.lanf.api.order.model.bo.DiscountInfoJson;
import com.lanf.api.order.model.bo.ShippingInfoBO;
import com.lanf.api.order.model.enums.OrderTypeEnum;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderDetailForAdminVO implements Serializable {

    private Long id ;

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

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 0:待付款, 1:待出库 2：已出库 3：已发货，4：已完成，5：已取消 6.已关闭
     */
    private OrderStatusEnum status;

    /**
     * 订单类型 0：普通订单 ,1:秒杀单
     */
    private OrderTypeEnum orderType;

    /**
     * 售后有效期，如果多个商品不同售后期，那么取最大的
     */
    private Integer afterSaleDays;

    private Date createTime;

    private Integer payType;

    private ShippingInfoBO shippingInfoBO;
    /**
     * 收货地址
     */
    private AddressJson takeAddressJson;
    /**
     * 优惠信息
     */
    private List<DiscountInfoJson> discountInfoBOS;

    private List<OrderItemVO> orderItemVOList;




}
