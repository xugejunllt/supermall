package com.lanf.finance.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.finance.model.entity.ClearingDetailDO;
import com.lanf.finance.model.enums.ClearingStatusEnum;
import com.lanf.finance.service.ClearingDetailService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 订单取消时 回滚结算单
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.CANCEL_ORDER_EVENT_TOPIC, consumerGroup = TopicName.CANCEL_ORDER_EVENT_FINANCE_GROUP)
public class CancelOrderEventRollbackClearingDetailListener implements RocketMQListener<CancelOrderEventMessage> {

    @Autowired
    private ClearingDetailService clearingDetailService;


    @Transactional
    @Override
    public void onMessage(CancelOrderEventMessage message) {
        log.info("取消订单事件回滚三方支付订单开始:[{{}}]", JsonUtils.toJsonString(message));

        Long orderId = message.getOrderId();
        List<ClearingDetailDO> listed = clearingDetailService.lambdaQuery()
                .eq(ClearingDetailDO::getOrderId, orderId).list();

        if (listed.isEmpty()){
            log.warn("结算单不存在");
            /**
             * 结算单异步插入 此时可能不存在 进行重试
             */
            throw new MessageRetryConsumeException("结算单不存在");
        }
        for (ClearingDetailDO clearingDetailDO : listed){
             if ( !ClearingStatusEnum.CLEARING.equals(clearingDetailDO.getStatus())){
                 throw new MessageRetryConsumeException("结算单状态异常");
             }
             boolean update = clearingDetailService.lambdaUpdate()
                     .eq(ClearingDetailDO::getId, clearingDetailDO.getId())
                     .eq(ClearingDetailDO::getStatus, ClearingStatusEnum.CLEARING)
                     .eq(ClearingDetailDO::getVersion, clearingDetailDO.getVersion())
                     .set(ClearingDetailDO::getStatus, ClearingStatusEnum.CANCELLED)
                     .set(ClearingDetailDO::getVersion, clearingDetailDO.getVersion() + 1)
                     .update();
             if (!update) {
                 throw new MessageRetryConsumeException("结算单更新失败");
             }
        }


    }


}