package com.lanf.pay.service.reconciliation.excel;


import com.alibaba.excel.annotation.ExcelProperty;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.BeanUtil;
import com.lanf.pay.mapper.SignCustomerFundBillDetailMapper;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 对账单 Excel 数据模型
 * 字段顺序需要与 Excel 表格列顺序一致
 */
@Slf4j
@Getter
@Setter
public class AalPaySignCustomerFundBillDetailExcel extends FundBillDetailReadListener<AalPaySignCustomerFundBillDetailExcel,
        SignCustomerFundBillDetailDO>{

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

    public AalPaySignCustomerFundBillDetailExcel() {
        super(null,null);
    }

    public AalPaySignCustomerFundBillDetailExcel(String batchId, String payChannel) {
        super(batchId, payChannel);
    }

    @Override
    SignCustomerFundBillDetailDO convertToDO(AalPaySignCustomerFundBillDetailExcel excel) {
        SignCustomerFundBillDetailDO detailDO = new SignCustomerFundBillDetailDO();

        detailDO.setMerchantOrderNo(excel.getMerchantOrderNo());
        detailDO.setFinancialSerialNo(excel.getFinancialSerialNo());
        detailDO.setBusinessSerialNo(excel.getBusinessSerialNo());
        detailDO.setCounterpartyAccount(excel.getCounterpartyAccount());
        detailDO.setTransactionChannel(excel.getTransactionChannel());
        ReconciliationBusinessTypeEnum byCode = ReconciliationBusinessTypeEnum.getByCode(Integer.parseInt(excel.getBusinessType()));
        detailDO.setBusinessType(byCode);
        detailDO.setRemark(excel.getRemark());

        // 转换金额
        detailDO.setIncomeAmount(parseBigDecimal(excel.getIncomeAmountStr()));
        detailDO.setExpenseAmount(parseBigDecimal(excel.getExpenseAmountStr()));
        detailDO.setAccountBalance(parseBigDecimal(excel.getAccountBalanceStr()));
        // 设置批次信息
        detailDO.setPayChannel( PayChannelEnum.getByCode(Integer.parseInt(super.payChannel)));
        detailDO.setPayFinishDate(super.batchId);
        // 转换时间
        detailDO.setOccurTime(parseLocalDateTime(excel.getOccurTimeStr()));

        return detailDO;
    }

    @Override
    void batchInsertIgnore(List<SignCustomerFundBillDetailDO> list) {

        SignCustomerFundBillDetailMapper detailMapper = BeanUtil.getBean(SignCustomerFundBillDetailMapper.class);
        detailMapper.batchInsertIgnore(list);

    }


}
