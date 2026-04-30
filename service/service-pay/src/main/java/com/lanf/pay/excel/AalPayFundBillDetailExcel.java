package com.lanf.pay.excel;


import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 对账单 Excel 数据模型
 * 字段顺序需要与 Excel 表格列顺序一致
 */
@Data
public class AalPayFundBillDetailExcel {

    @ExcelProperty(index = 0, value = "支付渠道")
    private String payChannel;

    @ExcelProperty(index = 1, value = "支付完成日期")
    private String payFinishDate;

    @ExcelProperty(index = 2, value = "商户订单号")
    private String merchantOrderNo;

    @ExcelProperty(index = 3, value = "财务流水号")
    private String financialSerialNo;

    @ExcelProperty(index = 4, value = "业务流水号")
    private String businessSerialNo;

    @ExcelProperty(index = 5, value = "发生时间")
    private String occurTimeStr;

    @ExcelProperty(index = 6, value = "对方账号")
    private String counterpartyAccount;

    @ExcelProperty(index = 7, value = "收入金额")
    private String incomeAmountStr;

    @ExcelProperty(index = 8, value = "支出金额")
    private String expenseAmountStr;

    @ExcelProperty(index = 9, value = "账户余额")
    private String accountBalanceStr;

    @ExcelProperty(index = 10, value = "交易渠道")
    private String transactionChannel;

    @ExcelProperty(index = 11, value = "业务类型")
    private String businessType;

    @ExcelProperty(index = 12, value = "备注")
    private String remark;
}
