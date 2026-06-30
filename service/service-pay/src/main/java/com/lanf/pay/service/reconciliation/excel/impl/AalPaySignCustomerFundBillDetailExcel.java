package com.lanf.pay.service.reconciliation.excel.impl;


import com.alibaba.excel.annotation.ExcelProperty;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.BeanUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.utils.IdUtils;
import com.lanf.pay.mapper.SignCustomerFundBillDetailMapper;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.api.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.service.reconciliation.excel.AbstractFundBillDetailReadListener;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 对账单 Excel 数据模型
 * 字段顺序需要与 Excel 表格列顺序一致
 */
@Slf4j
@Getter
@Setter
@ToString
public class AalPaySignCustomerFundBillDetailExcel extends AbstractFundBillDetailReadListener<AalPaySignCustomerFundBillDetailExcel,
        SignCustomerFundBillDetailDO> {

    private static List<String> breakKey = Arrays.asList("#", "账务流水号");

    @ExcelProperty(index = 0, value = "财务流水号")
    private String financialSerialNo;

    @ExcelProperty(index = 1, value = "业务流水号")
    private String businessSerialNo;

    @ExcelProperty(index = 2, value = "商户订单号")
    private String merchantOrderNo;

    @ExcelProperty(index = 3, value = "商品名称")
    private String goodsName;

    @ExcelProperty(index = 4, value = "发生时间")
    private String occurTimeStr;

    @ExcelProperty(index = 5, value = "对方账号")
    private String counterpartyAccount;

    @ExcelProperty(index = 6, value = "收入金额")
    private String incomeAmountStr;

    @ExcelProperty(index = 7, value = "支出金额")
    private String expenseAmountStr;

    @ExcelProperty(index = 8, value = "账户余额")
    private String accountBalanceStr;

    @ExcelProperty(index = 9, value = "交易渠道")
    private String transactionChannel;

    @ExcelProperty(index = 10, value = "业务类型")
    private String businessType;

    @ExcelProperty(index = 11, value = "备注")
    private String remark;

    public AalPaySignCustomerFundBillDetailExcel() {
        super(null, null);
    }

    public AalPaySignCustomerFundBillDetailExcel(String batchId, String payChannel) {
        super(batchId, payChannel);
    }

    @Override
    protected SignCustomerFundBillDetailDO convertToDO(AalPaySignCustomerFundBillDetailExcel excel) {

        String financialSerialNo1 = excel.getFinancialSerialNo();

        for (String s : breakKey){
            if (financialSerialNo1.contains(s)){
                return null;
            }

        }
        SignCustomerFundBillDetailDO detailDO = new SignCustomerFundBillDetailDO();

        detailDO.setMerchantOrderNo(excel.getMerchantOrderNo());
        detailDO.setFinancialSerialNo(excel.getFinancialSerialNo());
        detailDO.setBusinessSerialNo(excel.getBusinessSerialNo());
        detailDO.setCounterpartyAccount(excel.getCounterpartyAccount());
        detailDO.setTransactionChannel(excel.getTransactionChannel());
        ReconciliationBusinessTypeEnum byCode = getReconciliationBusinessTypeEnum(excel.businessType);
        detailDO.setBusinessType(byCode);
        detailDO.setRemark(excel.getRemark());

        // 转换金额
        detailDO.setIncomeAmount(parseBigDecimal(excel.getIncomeAmountStr()));
        detailDO.setExpenseAmount(parseBigDecimal(excel.getExpenseAmountStr()).abs());
        detailDO.setAccountBalance(parseBigDecimal(excel.getAccountBalanceStr()));
        // 设置批次信息
        detailDO.setPayChannel(PayChannelEnum.getByCode(Integer.parseInt(super.payChannel)));
        detailDO.setPayFinishDate(super.batchId);
        // 转换时间
        detailDO.setOccurTime(parseLocalDateTime(excel.getOccurTimeStr()));

        return detailDO;
    }

    private ReconciliationBusinessTypeEnum getReconciliationBusinessTypeEnum(String businessType) {
        if ("在线支付".equals(businessType)) {
            return ReconciliationBusinessTypeEnum.PAYMENT;
        }
        if ("转账".equals(businessType)) {
            return ReconciliationBusinessTypeEnum.TRANSFER;
        }
        if ("退款".equals(businessType)) {
            return ReconciliationBusinessTypeEnum.REFUND;
        }
        log.error("不支持的业务类型");
        throw new BizException("不支持的业务类型");

    }


    @Override
    protected void batchInsertIgnore(List<SignCustomerFundBillDetailDO> list) {

        SignCustomerFundBillDetailMapper billDetailMapper = BeanUtil.getBean(SignCustomerFundBillDetailMapper.class);
        list.forEach(al -> {
            Date date = new Date();
            al.setId(IdUtils.generateId());
            al.setCreateTime(date);
            al.setUpdateTime(date);
        });

        billDetailMapper.batchInsertIgnore(list);

    }


}
