package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.enums.OrderTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Data
@TableName("orders")
public class OrderDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long mainOrderId;

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

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 优惠信息
     */
    private String discountInfo;
    /**
     * 收货地址
     */
    private String takeAddress;
    /**
     * 0:待付款, 1:待出库 2：已出库 3：已发货，4：已完成，5：已取消 6.已关闭
     */
    private OrderStatusEnum status;

    /**
     * 订单类型 0：普通订单 ,1:秒杀单
     */
    private OrderTypeEnum orderType;
    /***
     * order_process_steps  当秒杀单时有值
     * 0：订单创建成功
     * 1：交易单创建成功
     * 2：库存扣减成功
     * 3：库存扣减失败
     */
    private String orderProcessSteps;

    /**
     * 售后有效期，如果多个商品不同售后期，那么取最大的
     */
    private Integer afterSaleDays;

    /**
     * 版本号
     */
    private Long version;

    private Long tenantId;
}
