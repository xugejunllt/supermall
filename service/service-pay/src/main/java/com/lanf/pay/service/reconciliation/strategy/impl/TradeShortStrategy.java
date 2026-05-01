package com.lanf.pay.service.reconciliation.strategy.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.bo.ReconciliationScanPage;
import com.lanf.pay.model.bo.ReconciliationScanPageResult;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.service.reconciliation.SignCustomerIFundBillDetailService;
import com.lanf.pay.service.reconciliation.strategy.AbstractReconciliationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易单短款扫描策略
 */
@Component
public class TradeShortStrategy extends AbstractReconciliationStrategy<SignCustomerFundBillDetailDO> {


    @Autowired
    private SignCustomerIFundBillDetailService fundBillDetailService;

    @Override
    public ReconciliationJobTypeEnum getJobType() {

        return ReconciliationJobTypeEnum.TRADE_SHORT_CHECK;
    }

    @Override
    protected ReconciliationScanPageResult<SignCustomerFundBillDetailDO> doPage(ReconciliationScanPage pages) {


        long currentPage = pages.getCurrentPage();
        long pageSize = pages.getPageSize();
        String bathId = pages.getBathId();

        Page<SignCustomerFundBillDetailDO> page = new Page<>(currentPage, pageSize);
        /**
         * 根据id排序
         */
        IPage<SignCustomerFundBillDetailDO> resultPage = fundBillDetailService.lambdaQuery()
                .eq(SignCustomerFundBillDetailDO::getPayFinishDate, bathId)
                .eq(SignCustomerFundBillDetailDO::getBusinessType, ReconciliationBusinessTypeEnum.PAYMENT)
                .orderByDesc(BaseEntity::getId)
                .page(page);

        ReconciliationScanPageResult<SignCustomerFundBillDetailDO> result = new ReconciliationScanPageResult<>();
        result.setDataList(resultPage.getRecords());
        result.setPages(resultPage.getPages());
        result.setOutTradeNoList(resultPage.getRecords().stream().map(SignCustomerFundBillDetailDO::getBusinessSerialNo)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    protected List<ReconciliationTradeInfo> buildTradeInfoList(List<SignCustomerFundBillDetailDO> dataList) {

        List<ReconciliationTradeInfo> tradeInfoList = new ArrayList<>();
        for (SignCustomerFundBillDetailDO orderFlow : dataList) {

            ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
            tradeInfo.setOutTradeNo(orderFlow.getMerchantOrderNo());
            tradeInfo.setReceiptMoney(orderFlow.getIncomeAmount());
            tradeInfo.setPayChannel(orderFlow.getPayChannel());
            tradeInfo.setReconciliationBusinessType(ReconciliationBusinessTypeEnum.PAYMENT);
            tradeInfoList.add(tradeInfo);
        }

        return tradeInfoList;

    }

    @Override
    protected ReconciliationDiffTypeEnum getDiffType() {

        return ReconciliationDiffTypeEnum.SHORT;
    }

    @Override
    protected ReconciliationBusinessTypeEnum getBusinessType() {

        return ReconciliationBusinessTypeEnum.PAYMENT;
    }
}
