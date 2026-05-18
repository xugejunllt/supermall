package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.PayOrderTradeStatusEnum;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 交易账单表
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
@Data
@TableName("trade_fund_bill_detail")
public class TradeFundBillDetailDO extends BaseEntity {



    @ApiModelProperty(value = "账单日期，即交易发生的日期")
    private String billDate;
    private PayChannelEnum payChannel;
    @ApiModelProperty(value = "支付宝为商家分配的身份标识")
    private String customerId;

    @ApiModelProperty(value = "合作伙伴ID（如有）")
    private String partnerId;

    @ApiModelProperty(value = "支付宝交易号，关联支付宝系统的唯一凭证")
    private String tradeNo;

    @ApiModelProperty(value = "商户订单号，商户系统中的订单唯一标识")
    private String outTradeNo;

    @ApiModelProperty(value = "商户退款单号，退款时对应商户系统的退款单号，关联原始out_trade_no")
    private String outRefundNo;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "手续费，商家为这笔交易付出的成本")
    private BigDecimal fee;

    @ApiModelProperty(value = "结算金额，实际进入账户的金额 (amount - fee)")
    private BigDecimal settlementAmount;

    @ApiModelProperty(value = "优惠金额，平台或商家让利的金额")
    private BigDecimal discountFee;
    @ApiModelProperty(value = "实收金额，买家实际支付的现金（不包含优惠）")
    private BigDecimal receiptAmount;

    private ReconciliationBusinessTypeEnum tradeType;

    private PayOrderTradeStatusEnum tradeStatus;

    @ApiModelProperty(value = "交易发起时间")
    private LocalDateTime tradeTime;

    @ApiModelProperty(value = "用户实际付款时间")
    private Date paymentTime;

    @ApiModelProperty(value = "资金结算时间")
    private LocalDateTime settlementTime;



    @ApiModelProperty(value = "商品标题")
    private String goodsTitle;

    @ApiModelProperty(value = "商品描述")
    private String goodsDescription;

    @ApiModelProperty(value = "买家支付宝账号")
    private String buyerAccount;

    @ApiModelProperty(value = "卖家支付宝账号")
    private String sellerAccount;



    @ApiModelProperty(value = "可开票金额")
    private BigDecimal invoiceAmount;

    @ApiModelProperty(value = "原始的CSV行数据，或者JSON格式，保留所有原始字段以备不时之需")
    private String rawData;

    @ApiModelProperty(value = "数据校验和，用于确保数据在导入过程中未损坏")
    private String checksum;




}
