package com.lanf.pay.mq.listener;

import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.ReconciliationDiffDO;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.reconciliation.IReconciliationDiffMarkerService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 开始对账任务
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.RECONCILIATION_START_TOPIC,
        consumerGroup = PayMqGroupName.RECONCILIATION_START_GROUP
)
public class ReconciliationStartListener implements RocketMQListener<ReconciliationStartMessage> {


    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private IPayOrderFlowService payOrderFlowService;

    @Autowired
    private IReconciliationDiffMarkerService reconciliationDiffMarkerService;


    @Override
    public void onMessage(ReconciliationStartMessage message) {

        ReconciliationJobTypeEnum jobType = message.getJobType();

        List<ReconciliationTradeInfo> reconciliationTradeInfoList =
                message.getReconciliationTradeInfoList();

        String bathId = message.getBathId();


        /**
         * 去重
         */
        List<String> outTradeNoList = reconciliationTradeInfoList.stream()
                .map(ReconciliationTradeInfo::getOutTradeNo).collect(Collectors.toList());

        Integer count = reconciliationDiffMarkerService.lambdaQuery()
                .eq(ReconciliationDiffMarkerDO::getBatchId, bathId)
                .eq(ReconciliationDiffMarkerDO::getDiffType, ReconciliationDiffTypeEnum.LONG)
                .eq(ReconciliationDiffMarkerDO::getBusinessType, ReconciliationBusinessTypeEnum.PAYMENT)
                .in(ReconciliationDiffMarkerDO::getBusinessOrderNo, outTradeNoList)
                .count();
        Integer size = outTradeNoList.size();
        if ( size.equals( count)){
            log.info("该批次已对账");
            return;
        }
        List<PayOrderFlowDO> list = payOrderFlowService.lambdaQuery()
                .in(PayOrderFlowDO::getOutTradeNo, outTradeNoList).list();


        Map<String, PayOrderFlowDO> payOrderFlowDOMap = list.stream()
                .filter(detail -> detail.getOutTradeNo() != null) // 过滤掉 merchantOrderNo 为 null 的记录
                .collect(Collectors.toMap(
                        PayOrderFlowDO::getOutTradeNo,
                        detail -> detail,
                        (existing, replacement) -> existing
                ));
        /**
         * 长款
         */
        List<ReconciliationDiffDO> longDiffList = new ArrayList<>();
        for (String outTradeNo : outTradeNoList){

            /**
             * 1.找出长款
             */
            PayOrderFlowDO payOrderFlowDO = payOrderFlowDOMap.get(outTradeNo);
            if (payOrderFlowDO == null){
                //短款
                ReconciliationDiffDO reconciliationDiffDO = new ReconciliationDiffDO();
                reconciliationDiffDO.setBatchId(bathId);
                reconciliationDiffDO.setBusinessOrderNo(outTradeNo);
                reconciliationDiffDO.setPayChannel(fundBillDetailDO.getPayChannel());
                reconciliationDiffDO.setExpectedAmount(fundBillDetailDO.getIncomeAmount());
            }

        }
        z



    }


}
