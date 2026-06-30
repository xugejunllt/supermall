package com.lanf.pay.service.reconciliation.strategy.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.api.pay.model.enums.*;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.bo.ReconciliationScanPage;
import com.lanf.pay.model.bo.ReconciliationScanPageResult;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.pay.model.enums.*;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.SignCustomerIFundBillDetailService;
import com.lanf.pay.service.reconciliation.strategy.AbstractReconciliationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易单长款扫描策略
 */
@Slf4j
@Component("tradeLongStrategy")
public class TradeLongStrategy extends AbstractReconciliationStrategy<SignCustomerFundBillDetailDO> {


    @Autowired
    private SignCustomerIFundBillDetailService signCustomerIFundBillDetailService ;
    @Autowired
    private IPayOrderFlowService payOrderFlowService ;


    @Override
    public ReconciliationJobTypeEnum getJobType() {

        return ReconciliationJobTypeEnum.TRADE_LONG_CHECK;
    }

    @Override
    protected ReconciliationScanPageResult<SignCustomerFundBillDetailDO> doPage(ReconciliationScanPage pages) {


        long currentPage = pages.getCurrentPage();
        long pageSize = pages.getPageSize();
        String bathId = pages.getBathId();
        Page<SignCustomerFundBillDetailDO> page = new Page<>(currentPage, pageSize);
        /**
         * 根据id升序
         */
        IPage<SignCustomerFundBillDetailDO> resultPage =
                signCustomerIFundBillDetailService.lambdaQuery()
                .eq(SignCustomerFundBillDetailDO::getPayFinishDate, bathId)
                .eq(SignCustomerFundBillDetailDO::getBusinessType, ReconciliationBusinessTypeEnum.PAYMENT)
                .orderByAsc(BaseEntity::getId)
                .page(page);

        ReconciliationScanPageResult<SignCustomerFundBillDetailDO> result = new ReconciliationScanPageResult<>();
        result.setDataList(resultPage.getRecords());
        result.setPages(resultPage.getPages());


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
            tradeInfo.setReconciliationTradeStatus(toReconciliationTradeStatus(orderFlow));
            tradeInfo.setPayFinishTime(orderFlow.getPayFinishDate());
            tradeInfo.setId(orderFlow.getId());
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

    @Override
    protected ReconciliationTradeStatusEnum toReconciliationTradeStatus(SignCustomerFundBillDetailDO data) {



        return ReconciliationTradeStatusEnum.SUCCESS;
    }

    @Override
    protected Map<String, ReconciliationTradeInfo> toReconciliationTradeInfoMap(List<String> outTradeNoList) {

        List<PayOrderFlowDO> list = payOrderFlowService.lambdaQuery()
                .in(PayOrderFlowDO::getOutTradeNo, outTradeNoList).list();
        Map<String, ReconciliationTradeInfo> tradeInfoMap =  new HashMap<>();
        for (PayOrderFlowDO payOrderFlowDO : list) {

            PayOrderFlowStatusEnum status = payOrderFlowDO.getStatus();

            ReconciliationTradeStatusEnum reconciliationTradeStatus = null;
            if (status == PayOrderFlowStatusEnum.SUCCESS){
                reconciliationTradeStatus = ReconciliationTradeStatusEnum.SUCCESS;
            } else {
                reconciliationTradeStatus = ReconciliationTradeStatusEnum.FAILED;
            }

            ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
            tradeInfo.setOutTradeNo(payOrderFlowDO.getOutTradeNo());
            tradeInfo.setPayChannel(PayChannelEnum.getByCode(payOrderFlowDO.getPayType()));
            tradeInfo.setReceiptMoney(payOrderFlowDO.getReceiptMoney());
            tradeInfo.setReconciliationTradeStatus(reconciliationTradeStatus);
            tradeInfoMap.put(payOrderFlowDO.getOutTradeNo(), tradeInfo);
        }


        return tradeInfoMap;
    }
}
