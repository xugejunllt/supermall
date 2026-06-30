package com.lanf.pay.model.vo;


import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.pay.model.enums.ReconciliationTradeStatusEnum;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ReconciliationDiffPageVO implements Serializable {


    private Long id;
    /**
     * 批次号，如 2026-04-29
     */
    private String batchId;

    /**
     * 业务单号（商户订单号）
     */
    private String businessOrderNo;

    /**
     * 支付渠道
     */
    private PayChannelEnum payChannel;

    /**
     * 预期金额（我方金额）
     */
    private BigDecimal expectedAmount;

    /**
     * 实际金额（渠道金额）
     */
    private BigDecimal actualAmount;

    /**
     * 我方状态 0:交易成功 1：交易失败
     */
    private ReconciliationTradeStatusEnum expectedStatus;
    /**
     * 三方状态 0:交易成功 1：交易失败
     */
    private ReconciliationTradeStatusEnum actualStatus;

    /**
     * 差异金额（实际金额 - 预期金额）
     */
    private BigDecimal diffAmount;

    /**
     * 0：长款,1：短款，2：金额不符
     */
    private ReconciliationDiffTypeEnum diffType;



    private ReconciliationBusinessTypeEnum businessType;

    private Date createTime;
}
