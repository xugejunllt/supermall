package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.enums.AfterSalesTypeEnum;
import com.lanf.order.model.enums.IncomeStatusEnum;
import com.lanf.order.model.enums.MainStatusEnum;
import com.lanf.order.model.enums.SubStatus;
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
    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 售后单编号
     */
    private String orderNumber;

    private AfterSalesTypeEnum afterSalesType;

    /**
     *
     */
    private MainStatusEnum mainStatus;

    /**
     * 子状态
     */
    private SubStatus subStatus;

    /**
     * 商家自动同意时间
     */
    private Date businessAutoAgreeTime;

    /**
     * 快递编号
     */
    private String expressNumber;
    /**
     * 快递公司
     */
    private String expressCompany;

    /**
     * 退款原因
     */
    private String returnReason;

    /**
     * 退款金额
     */
    private BigDecimal returnMoney;

    private IncomeStatusEnum incomeStatus;

    private Long version;
    private Long tenantId;
}
