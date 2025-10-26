package com.lanf.aftersales.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class AfterSalesOrderPageVO implements Serializable {

    private Long id;
    private Long shopId;
    private String shopName;

    @ApiModelProperty(value = "售后类型 0:退货退款 1:换货")
    private Integer afterSalesType;

    private Integer incomeStatus;
    private String afterSalesTypeName;
    //退款状态名称
    private String incomeStatusName;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal returnMoney;

    @ApiModelProperty(value = "快递编号")
    private String expressNumber;

    private String expressCompany;
    @ApiModelProperty(value = "退款原因")
    private String returnReason;
    /**
     * 退货退款状态
     * 0.已发布，商家处理中
     * 1.商家同意申请，买家处理中
     * 2.买家已发货，待商家收货
     * 3.商家收货，售后完成
     * 4.售后关闭
     */
    private Integer returnsAndRefundsStatus;

    private String returnsAndRefundsStatusName;

    @ApiModelProperty(value = "商家自动同意时间")
    private Date businessAutoAgreeTime;

    @ApiModelProperty(value = "售后单编号")
    private String orderNumber;



    private List<AfterSalesOrderItemPageVO> afterSalesOrderItemPageVOS;


}
