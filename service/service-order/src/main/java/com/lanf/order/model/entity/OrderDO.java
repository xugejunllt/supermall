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

    private static final long serialVersionUID = 1L;


    private Long mainOrderId;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "订单编号")
    private String orderNumber;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal totalMoney;

    @ApiModelProperty(value = "实付金额")
    private BigDecimal actualPayMoney;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "优惠信息")
    private String discountInfo;
    @ApiModelProperty(value = "收货地址")
    private String takeAddress;
    @ApiModelProperty(value = "0:待付款, 1:待出库 2：已出库 3：已发货，4：已完成，5：已取消 6.已关闭")
    private Integer status;

    /**
     * 0：正常,1: 冻结状态 用于分布式事务try阶段更新，
     * 其他非分布式事务场景，状态是正常状态允许更新
     */
    private Integer frozen;
    /**
     * 售后有效期，如果多个商品不同售后期，那么取最大的
     */
    private Integer afterSaleDays;

    @ApiModelProperty(value = "版本号")
    private Long version;


}
