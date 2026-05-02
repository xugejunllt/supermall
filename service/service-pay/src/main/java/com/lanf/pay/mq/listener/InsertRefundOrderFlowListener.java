package com.lanf.pay.mq.listener;

/**
 * 插入流水
 */

import com.lanf.common.utils.DateUtils;
import com.lanf.pay.model.entity.RefundOrderFlowDO;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.RefundQueryResultProcessorMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.pay.IRefundOrderFlowService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.REFUND_QUERY_RESULT_TOPIC,
        consumerGroup = PayMqGroupName.INSERT_REFUND_ORDER_FLOW_GROUP
)
public class InsertRefundOrderFlowListener implements RocketMQListener<RefundQueryResultProcessorMessage> {

    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private IRefundOrderFlowService refundOrderFlowService;

    @Override
    public void onMessage(RefundQueryResultProcessorMessage message) {


        String outTradeNo = message.getOutTradeNo();
        /**
         * 插入流水
         */
        RefundOrderFlowDO one = refundOrderFlowService.lambdaQuery()
                .eq(RefundOrderFlowDO::getOutTradeNo, outTradeNo)
                //全部退款 OutRequestNo = outTradeNo
                .eq(RefundOrderFlowDO::getOutRequestNo, outTradeNo)
                .one();
        if (one != null) {

            log.info("退款流水已存在");
            return;
        }
        RefundOrderFlowDO refundOrderFlowDO = new RefundOrderFlowDO();
        refundOrderFlowDO.setOutTradeNo(outTradeNo);
        refundOrderFlowDO.setOutRequestNo(outTradeNo);
        refundOrderFlowDO.setTradeNo(message.getTradeNo());
        refundOrderFlowDO.setPayMoney(message.getPayMoney());
        refundOrderFlowDO.setReturnMoney(message.getReturnMoney());
        refundOrderFlowDO.setStatus(message.getStatus());
        refundOrderFlowDO.setPayChannelEnum(message.getPayChannelEnum());
        refundOrderFlowDO.setPayFinishTime(message.getPayFinishTime());
        refundOrderFlowDO.setPayFinishDate(DateUtils.format(message.getPayFinishTime(),
                DateUtils.DATE));
        refundOrderFlowDO.setFailReason(message.getFailReason());

        try {
            refundOrderFlowService.save(refundOrderFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("退款流水已存在");

        }
    }
}
