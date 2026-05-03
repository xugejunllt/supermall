package com.lanf.pay.service.reconciliation.excel.impl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.lanf.common.utils.BeanUtil;
import com.lanf.pay.mapper.TradeFundBillDetailMapper;
import com.lanf.pay.model.entity.TradeFundBillDetailDO;
import com.lanf.pay.service.reconciliation.excel.AbstractFundBillDetailReadListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 交易资金账单详情 Excel 数据模型
 * 字段顺序需要与 Excel 表格列顺序一致
 */
@Slf4j
@Getter
@Setter
public class AalPayTradeFundBillDetailExcel extends AbstractFundBillDetailReadListener<AalPayTradeFundBillDetailExcel, TradeFundBillDetailDO> {

    @ExcelProperty(index = 0, value = "账单日期")
    private String billDate;

    @ExcelProperty(index = 1, value = "客户ID")
    private String customerId;

    @ExcelProperty(index = 2, value = "合作伙伴ID")
    private String partnerId;

    @ExcelProperty(index = 3, value = "支付宝交易号")
    private String tradeNo;

    @ExcelProperty(index = 4, value = "商户订单号")
    private String outTradeNo;

    @ExcelProperty(index = 5, value = "商户退款单号")
    private String outRefundNo;

    @ExcelProperty(index = 6, value = "交易金额")
    private String amountStr;

    @ExcelProperty(index = 7, value = "手续费")
    private String feeStr;

    @ExcelProperty(index = 8, value = "结算金额")
    private String settlementAmountStr;

    @ExcelProperty(index = 9, value = "优惠金额")
    private String discountFeeStr;

    @ExcelProperty(index = 10, value = "交易类型")
    private String tradeType;

    @ExcelProperty(index = 11, value = "交易状态")
    private String tradeStatus;

    @ExcelProperty(index = 12, value = "交易发起时间")
    private String tradeTimeStr;

    @ExcelProperty(index = 13, value = "用户付款时间")
    private String paymentTimeStr;

    @ExcelProperty(index = 14, value = "资金结算时间")
    private String settlementTimeStr;

    @ExcelProperty(index = 15, value = "商品标题")
    private String goodsTitle;

    @ExcelProperty(index = 16, value = "商品描述")
    private String goodsDescription;

    @ExcelProperty(index = 17, value = "买家账号")
    private String buyerAccount;

    @ExcelProperty(index = 18, value = "卖家账号")
    private String sellerAccount;

    @ExcelProperty(index = 19, value = "实收金额")
    private String receiptAmountStr;

    @ExcelProperty(index = 20, value = "可开票金额")
    private String invoiceAmountStr;

    public AalPayTradeFundBillDetailExcel() {
        super(null, null);
    }

    public AalPayTradeFundBillDetailExcel(String batchId, String payChannel) {
        super(batchId, payChannel);
    }

    @Override
    protected TradeFundBillDetailDO convertToDO(AalPayTradeFundBillDetailExcel excel) {
        TradeFundBillDetailDO detail = new TradeFundBillDetailDO();

        detail.setBillDate(excel.getBillDate());
        detail.setCustomerId(excel.getCustomerId());
        detail.setPartnerId(excel.getPartnerId());
        detail.setTradeNo(excel.getTradeNo());
        detail.setOutTradeNo(excel.getOutTradeNo());
        detail.setOutRefundNo(excel.getOutRefundNo());
        
        // 转换金额
        detail.setAmount(parseBigDecimal(excel.getAmountStr()));
        detail.setFee(parseBigDecimal(excel.getFeeStr()));
        detail.setSettlementAmount(parseBigDecimal(excel.getSettlementAmountStr()));
        detail.setDiscountFee(parseBigDecimal(excel.getDiscountFeeStr()));
        detail.setReceiptAmount(parseBigDecimal(excel.getReceiptAmountStr()));
        detail.setInvoiceAmount(parseBigDecimal(excel.getInvoiceAmountStr()));
//
//        detail.setTradeType(excel.getTradeType());
//        detail.setTradeStatus(excel.getTradeStatus());
        
        // 转换时间
//        detail.setTradeTime(parseLocalDateTime(excel.getTradeTimeStr()));
//        detail.setPaymentTime(parseLocalDateTime(excel.getPaymentTimeStr()));
//        detail.setSettlementTime(parseLocalDateTime(excel.getSettlementTimeStr()));
        
        detail.setGoodsTitle(excel.getGoodsTitle());
        detail.setGoodsDescription(excel.getGoodsDescription());
        detail.setBuyerAccount(excel.getBuyerAccount());
        detail.setSellerAccount(excel.getSellerAccount());
        

        return detail;
    }

    @Override
   protected   void batchInsertIgnore(List<TradeFundBillDetailDO> list) {
        TradeFundBillDetailMapper detailMapper = BeanUtil.getBean(TradeFundBillDetailMapper.class);
        detailMapper.batchInsertIgnore(list);
    }
}
