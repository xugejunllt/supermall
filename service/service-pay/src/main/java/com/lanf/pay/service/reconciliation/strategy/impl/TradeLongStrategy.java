package com.lanf.pay.service.reconciliation.strategy.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.DateUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.bo.ReconciliationScanPage;
import com.lanf.pay.model.bo.ReconciliationScanPageResult;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.TradeFundBillDetail;
import com.lanf.pay.model.enums.*;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.ITradeFundBillDetailService;
import com.lanf.pay.service.reconciliation.strategy.AbstractReconciliationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 交易单长款扫描策略
 */
@Component
public class TradeLongStrategy extends AbstractReconciliationStrategy<TradeFundBillDetail> {




    @Autowired
    private ITradeFundBillDetailService tradeFundBillDetailService ;
    @Autowired
    private IPayOrderFlowService payOrderFlowService ;


    @Override
    public ReconciliationJobTypeEnum getJobType() {

        return ReconciliationJobTypeEnum.TRADE_LONG_CHECK;
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

            Date paymentTime = orderFlow.getPaymentTime();
            String payFinishTime = null;
            if (paymentTime != null) {
                payFinishTime = DateUtils.format(paymentTime, DateUtils.DATE_TIME);
            }

            ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
            tradeInfo.setOutTradeNo(orderFlow.getOutTradeNo());
            tradeInfo.setReceiptMoney(orderFlow.getReceiptAmount());
            tradeInfo.setPayChannel(orderFlow.getPayChannel());
            tradeInfo.setReconciliationTradeStatus(toReconciliationTradeStatus(orderFlow));
            tradeInfo.setPayFinishTime(payFinishTime);
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
    protected ReconciliationTradeStatusEnum toReconciliationTradeStatus(TradeFundBillDetail data) {

        PayOrderTradeStatusEnum tradeStatus = data.getTradeStatus();
        if (tradeStatus == PayOrderTradeStatusEnum.TRADE_SUCCESS) {
            /**
             * 该状态为成功 其他的为失败
             */
            return ReconciliationTradeStatusEnum.SUCCESS;
        }

        return ReconciliationTradeStatusEnum.FAILED;
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
