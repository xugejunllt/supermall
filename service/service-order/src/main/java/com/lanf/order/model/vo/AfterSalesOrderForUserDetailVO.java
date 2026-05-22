package com.lanf.order.model.vo;

import com.lanf.order.model.enums.AfterSalesTypeEnum;
import com.lanf.order.model.enums.IncomeStatusEnum;
import com.lanf.order.model.enums.MainStatusEnum;
import com.lanf.order.model.enums.SubStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class AfterSalesOrderForUserDetailVO implements Serializable {

    private Long id;

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

    private List<AfterSalesOrderItemVO> afterSalesOrderItemVOList;


}
