package com.lanf.aftersales.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 售后单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-19
 */
@Data
@TableName("after_sales_order")
public class AfterSalesOrderDO extends BaseEntity {

    private static final long serialVersionUID = 1L;
    private Long userId;
    @ApiModelProperty(value = "订单id")
    private Long orderId;

    @ApiModelProperty(value = "售后单编号")
    private String orderNumber;

    private Long shopId;

    @ApiModelProperty(value = "售后类型 0:退货退款 1:换货")
    private Integer afterSalesType;

    /**
     * 退货退款状态
     * 0.已发布，商家处理中
     * 1.商家同意申请，买家处理中
     * 2.商家拒绝申请
     * 3.买家已发货，待商家收货
     * 4.商家收货，售后完成
     * 5.商家拒绝收货
     * 6.售后关闭（评价完成后关闭）
     * 7.已撤销
     */
    private Integer returnsAndRefundsStatus;

    @ApiModelProperty(value = "商家自动同意时间")
    private Date businessAutoAgreeTime;

    @ApiModelProperty(value = "申请时间")
    private Date applicationTime;

    @ApiModelProperty(value = "快递编号")
    private String expressNumber;

    private String expressCompany;

    @ApiModelProperty(value = "退款原因")
    private String returnReason;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal returnMoney;

    @ApiModelProperty(value = "退货数量")
    private Integer returnQuantity;
    //退款金额是否到账 0:未到账，1:已到账
    private Integer incomeStatus;

}
