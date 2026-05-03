package com.lanf.pay.service.reconciliation.strategy.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.bo.ReconciliationScanPage;
import com.lanf.pay.model.bo.ReconciliationScanPageResult;
import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.model.enums.ReconciliationTradeStatusEnum;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.strategy.AbstractReconciliationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交易单长款扫描策略
 */
@Component
public class TradeShortStrategy extends AbstractReconciliationStrategy<PayOrderFlowDO> {

    @Autowired
    private IPayOrderFlowService payOrderFlowService;


    @Override
    public ReconciliationJobTypeEnum getJobType() {
        return ReconciliationJobTypeEnum.TRADE_LONG_CHECK;
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
                .orderByDesc(BaseEntity::getId)
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

            ReconciliationTradeInfo tradeInfo = new ReconciliationTradeInfo();
            tradeInfo.setOutTradeNo(orderFlow.getOutTradeNo());
            tradeInfo.setReceiptMoney(orderFlow.getReceiptMoney());
            tradeInfo.setPayChannel(PayChannelEnum.getByCode(orderFlow.getPayType()));
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
    protected ReconciliationTradeStatusEnum toReconciliationTradeStatus(PayOrderFlowDO data) {
        return null;
    }

    @Override
    protected Map<String, ReconciliationTradeInfo> toReconciliationTradeInfoMap(List<String> outTradeNoList) {
        return null;
    }
}
