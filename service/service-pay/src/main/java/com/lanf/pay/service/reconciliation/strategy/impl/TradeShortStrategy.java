package com.lanf.pay.service.reconciliation.strategy.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.DateUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 交易单长款扫描策略
 */
@Component("tradeShortStrategy")
public class TradeShortStrategy extends AbstractReconciliationStrategy<PayOrderFlowDO> {

    @Autowired
    private IPayOrderFlowService payOrderFlowService;

    @Autowired
    private SignCustomerIFundBillDetailService signCustomerIFundBillDetailService ;
    @Override
    public ReconciliationJobTypeEnum getJobType() {
        return ReconciliationJobTypeEnum.TRADE_SHORT_CHECK;
    }

    @Override
    protected ReconciliationScanPageResult<PayOrderFlowDO> doPage(ReconciliationScanPage pages) {


        long currentPage = pages.getCurrentPage();
        long pageSize = pages.getPageSize();
        String bathId = pages.getBathId();

        Page<PayOrderFlowDO> page = new Page<>(currentPage, pageSize);
        /**
         * 根据id排序
         */
        IPage<PayOrderFlowDO> resultPage = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getPayFinishDate, bathId)
                .orderByAsc(BaseEntity::getId)
                .page(page);

        List<PayOrderFlowDO> orderFlowList = resultPage.getRecords();
        /**
         * 构建返回结果
         */
        ReconciliationScanPageResult<PayOrderFlowDO> result = new ReconciliationScanPageResult<>();
        result.setDataList(orderFlowList);
        result.setPages(resultPage.getPages());

        return result;
    }

    @Override
    protected List<ReconciliationTradeInfo> buildTradeInfoList(List<PayOrderFlowDO> dataList) {

        List<ReconciliationTradeInfo> tradeInfoList = new ArrayList<>();
        for (PayOrderFlowDO orderFlow : dataList) {

            Date paymentTime = orderFlow.getPayFinishTime();
            String payFinishTime = null;
            if (paymentTime != null) {
                payFinishTime = DateUtils.format(paymentTime, DateUtils.DATE_TIME);
            }

            ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
            tradeInfo.setOutTradeNo(orderFlow.getOutTradeNo());
            tradeInfo.setReceiptMoney(orderFlow.getReceiptMoney());
            tradeInfo.setPayChannel(PayChannelEnum.getByCode(orderFlow.getPayType()));
            tradeInfo.setReconciliationTradeStatus(toReconciliationTradeStatus(orderFlow));
            tradeInfo.setPayFinishTime(payFinishTime);
            tradeInfo.setId(orderFlow.getId());
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

    @Override
    protected ReconciliationTradeStatusEnum toReconciliationTradeStatus(PayOrderFlowDO data) {

        PayOrderFlowStatusEnum status = data.getStatus();
        if (status == PayOrderFlowStatusEnum.SUCCESS) {
            return ReconciliationTradeStatusEnum.SUCCESS;
        }

        return ReconciliationTradeStatusEnum.FAILED;
    }

    @Override
    protected Map<String, ReconciliationTradeInfo> toReconciliationTradeInfoMap(List<String> outTradeNoList) {

        List<SignCustomerFundBillDetailDO> list = signCustomerIFundBillDetailService.lambdaQuery()
                .in(SignCustomerFundBillDetailDO::getMerchantOrderNo, outTradeNoList).list();

        Map<String, ReconciliationTradeInfo> tradeInfoMap =  new HashMap<>();
        for (SignCustomerFundBillDetailDO payOrderFlowDO : list) {
            ReconciliationTradeInfo tradeInfo = getReconciliationTradeInfo(payOrderFlowDO);
            tradeInfoMap.put(payOrderFlowDO.getMerchantOrderNo(), tradeInfo);
        }


        return tradeInfoMap;
    }


    private static ReconciliationTradeInfo getReconciliationTradeInfo(SignCustomerFundBillDetailDO payOrderFlowDO) {

        ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
        tradeInfo.setOutTradeNo(payOrderFlowDO.getMerchantOrderNo());
        tradeInfo.setPayChannel(payOrderFlowDO.getPayChannel());
        tradeInfo.setReceiptMoney(payOrderFlowDO.getIncomeAmount());
        tradeInfo.setReconciliationTradeStatus(ReconciliationTradeStatusEnum.SUCCESS);
        return tradeInfo;
    }
}
