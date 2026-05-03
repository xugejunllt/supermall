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
import com.lanf.pay.model.entity.TradeFundBillDetailDO;
import com.lanf.pay.model.enums.*;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.ITradeFundBillDetailService;
import com.lanf.pay.service.reconciliation.strategy.AbstractReconciliationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 交易单长款扫描策略
 */
@Component
public class TradeLongStrategy extends AbstractReconciliationStrategy<TradeFundBillDetailDO> {




    @Autowired
    private ITradeFundBillDetailService tradeFundBillDetailService ;
    @Autowired
    private IPayOrderFlowService payOrderFlowService ;


    @Override
    public ReconciliationJobTypeEnum getJobType() {

        return ReconciliationJobTypeEnum.TRADE_LONG_CHECK;
    }

    @Override
    protected ReconciliationScanPageResult<TradeFundBillDetailDO> doPage(ReconciliationScanPage pages) {


        long currentPage = pages.getCurrentPage();
        long pageSize = pages.getPageSize();
        String bathId = pages.getBathId();

        Page<TradeFundBillDetailDO> page = new Page<>(currentPage, pageSize);
        /**
         * 根据id升序
         */
        IPage<TradeFundBillDetailDO> resultPage = tradeFundBillDetailService.lambdaQuery()
                .eq(TradeFundBillDetailDO::getBillDate, bathId)
                .eq(TradeFundBillDetailDO::getTradeType, ReconciliationBusinessTypeEnum.PAYMENT.getCode().toString())
                .orderByAsc(BaseEntity::getId)
                .page(page);

        ReconciliationScanPageResult<TradeFundBillDetailDO> result = new ReconciliationScanPageResult<>();
        result.setDataList(resultPage.getRecords());
        result.setPages(resultPage.getPages());


        return result;
    }

    @Override
    protected List<ReconciliationTradeInfo> buildTradeInfoList(List<TradeFundBillDetailDO> dataList) {

        List<ReconciliationTradeInfo> tradeInfoList = new ArrayList<>();
        for (TradeFundBillDetailDO orderFlow : dataList) {

            Date paymentTime = orderFlow.getPaymentTime();
            String payFinishTime = null;
            if (paymentTime != null) {
                payFinishTime = DateUtils.format(paymentTime, DateUtils.DATE_TIME);
            }

            ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
            tradeInfo.setOutTradeNo(orderFlow.getOutTradeNo());
            tradeInfo.setReceiptMoney(orderFlow.getSettlementAmount());
            tradeInfo.setPayChannel(orderFlow.getPayChannel());
            tradeInfo.setReconciliationTradeStatus(toReconciliationTradeStatus(orderFlow));
            tradeInfo.setPayFinishTime(payFinishTime);
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
    protected ReconciliationTradeStatusEnum toReconciliationTradeStatus(TradeFundBillDetailDO data) {

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
