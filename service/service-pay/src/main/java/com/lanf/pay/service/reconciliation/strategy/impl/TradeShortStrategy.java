package com.lanf.pay.service.reconciliation.strategy.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.bo.ReconciliationScanPage;
import com.lanf.pay.model.bo.ReconciliationScanPageResult;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.FundBillDetailDO;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.service.reconciliation.IFundBillDetailService;
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
public class TradeShortStrategy extends AbstractReconciliationStrategy<FundBillDetailDO> {


    @Autowired
    private IFundBillDetailService fundBillDetailService;

    @Override
    protected ReconciliationJobTypeEnum getJobType() {

        return ReconciliationJobTypeEnum.TRADE_SHORT_CHECK;
    }

    @Override
    protected ReconciliationScanPageResult<FundBillDetailDO> doPage(ReconciliationScanPage pages) {


        long currentPage = pages.getCurrentPage();
        long pageSize = pages.getPageSize();
        String bathId = pages.getBathId();

        Page<FundBillDetailDO> page = new Page<>(currentPage, pageSize);
        /**
         * 根据id排序
         */
        IPage<FundBillDetailDO> resultPage = fundBillDetailService.lambdaQuery()
                .eq(FundBillDetailDO::getPayFinishDate, bathId)
                .eq(FundBillDetailDO::getBusinessType, ReconciliationBusinessTypeEnum.PAYMENT)
                .orderByDesc(BaseEntity::getId)
                .page(page);

        ReconciliationScanPageResult<FundBillDetailDO> result = new ReconciliationScanPageResult<>();
        result.setDataList(resultPage.getRecords());
        result.setPages(resultPage.getPages());
        result.setOutTradeNoList(resultPage.getRecords().stream().map(FundBillDetailDO::getBusinessSerialNo)
                .collect(Collectors.toList()));

        return null;
    }

    @Override
    protected List<ReconciliationTradeInfo> buildTradeInfoList(List<FundBillDetailDO> dataList) {

        List<ReconciliationTradeInfo> tradeInfoList = new ArrayList<>();
        for (FundBillDetailDO orderFlow : dataList) {

            ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
            tradeInfo.setOutTradeNo(orderFlow.getMerchantOrderNo());
            tradeInfo.setReceiptMoney(orderFlow.getIncomeAmount());
            tradeInfo.setPayChannel(orderFlow.getPayChannel());
            tradeInfo.setReconciliationBusinessType(ReconciliationBusinessTypeEnum.PAYMENT);
            tradeInfoList.add(tradeInfo);
        }

        return tradeInfoList;

    }
}
