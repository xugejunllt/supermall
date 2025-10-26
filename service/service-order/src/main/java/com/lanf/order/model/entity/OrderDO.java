package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
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

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "店铺id")
    private Long shopId;


    /**
     *订单状态
     * 0:待付款, 1:待出库 2：已出库 3：已发货，4：已完成，5：已关闭 6.已取消
     *
     */
    private Integer status;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "订单编号")
    private String orderNumber;

    @ApiModelProperty(value = "订单总金额")
    private BigDecimal totalMoney;
    //实际支付金额
    private BigDecimal actualPayMoney;
    @ApiModelProperty(value = "收货地址")
    private String takeAddress;

    private String createTimeFormat;

}
