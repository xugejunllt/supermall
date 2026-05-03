package com.lanf.pay.service.reconciliation.strategy.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.bo.ReconciliationScanPage;
import com.lanf.pay.model.bo.ReconciliationScanPageResult;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.TradeFundBillDetail;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.service.reconciliation.ITradeFundBillDetailService;
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
public class TradeLongStrategy extends AbstractReconciliationStrategy<TradeFundBillDetail> {




    @Autowired
    private ITradeFundBillDetailService tradeFundBillDetailService ;

    @Override
    public ReconciliationJobTypeEnum getJobType() {

        return ReconciliationJobTypeEnum.TRADE_SHORT_CHECK;
    }

    @Override
    protected ReconciliationScanPageResult<TradeFundBillDetail> doPage(ReconciliationScanPage pages) {


        long currentPage = pages.getCurrentPage();
        long pageSize = pages.getPageSize();
        String bathId = pages.getBathId();

        Page<TradeFundBillDetail> page = new Page<>(currentPage, pageSize);
        /**
         * 根据id排序
         */
        IPage<TradeFundBillDetail> resultPage = tradeFundBillDetailService.lambdaQuery()
                .eq(TradeFundBillDetail::getBillDate, bathId)
                .eq(TradeFundBillDetail::getTradeType, ReconciliationBusinessTypeEnum.PAYMENT.getCode().toString())
                .orderByDesc(BaseEntity::getId)
                .page(page);

        ReconciliationScanPageResult<TradeFundBillDetail> result = new ReconciliationScanPageResult<>();
        result.setDataList(resultPage.getRecords());
        result.setPages(resultPage.getPages());
        result.setOutTradeNoList(resultPage.getRecords().stream().map(TradeFundBillDetail::getOutTradeNo)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    protected List<ReconciliationTradeInfo> buildTradeInfoList(List<TradeFundBillDetail> dataList) {

        List<ReconciliationTradeInfo> tradeInfoList = new ArrayList<>();
        for (TradeFundBillDetail orderFlow : dataList) {

            ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
            tradeInfo.setOutTradeNo(orderFlow.getOutTradeNo());
            tradeInfo.setReceiptMoney(orderFlow.getReceiptAmount());
            tradeInfo.setPayChannel(orderFlow.getPayChannel());
            tradeInfo.setTradeStatus(orderFlow.getTradeStatus());
            tradeInfo.setTradeMoney(orderFlow.getAmount());
            tradeInfo.setReconciliationBusinessType(ReconciliationBusinessTypeEnum.PAYMENT);
            tradeInfoList.add(tradeInfo);
        }

        return tradeInfoList;

    }

    @Override
    protected ReconciliationDiffTypeEnum getDiffType() {

        return ReconciliationDiffTypeEnum.LONG;
    }

    @Override
    protected ReconciliationBusinessTypeEnum getBusinessType() {

        return ReconciliationBusinessTypeEnum.PAYMENT;
    }
}
