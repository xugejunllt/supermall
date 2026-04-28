package com.lanf.finance.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.finance.model.entity.ClearingDetailDO;
import com.lanf.finance.mq.constant.FinanceMqGroupName;
import com.lanf.finance.service.ClearingDetailService;
import com.lanf.order.mq.OrderClientTopicName;
import com.lanf.order.mq.message.SignOrderMessage;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * 订单签收时 更新结算单 的售后过期时间
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.SIGN_ORDER_EVENT_TOPIC, consumerGroup = FinanceMqGroupName.SIGN_ORDER_EVENT_FINANCE_GROUP)
public class SignOrderEventUpdateSaleExpireTimeListener implements RocketMQListener<SignOrderMessage> {

    @Autowired
    private ClearingDetailService clearingDetailService;

    @Transactional
    @Override
    public void onMessage(SignOrderMessage message) {

        log.info("订单签收时开始:[{{}}]", JsonUtils.toJsonString(message));
        Long orderId = message.getOrderId();
        Date signTime = message.getSignTime();
        Integer afterSaleDays = message.getAfterSaleDays();
        Date afterSaleExpireTime = new Date(signTime.getTime() + afterSaleDays * 24 * 60 * 60 * 1000);
        List<ClearingDetailDO> listed = clearingDetailService.lambdaQuery()
                .eq(ClearingDetailDO::getOrderId, orderId).list();

        if (listed.isEmpty()){
            log.warn("结算单不存在");
            /**
             * 结算单异步插入 此时可能不存在 进行重试
             */
            throw new MessageRetryConsumeException("结算单不存在");
        }
        boolean allAfterSaleExpireTimeNotNull = listed.stream()
                .allMatch(detail -> detail.getAfterSaleExpireTime() != null);

        if (allAfterSaleExpireTimeNotNull) {
            log.info("订单 {} 的所有结算明细售后过期时间均已设置，无需更新", orderId);
            return;
        }

        for (ClearingDetailDO clearingDetailDO : listed){


            boolean update = clearingDetailService.lambdaUpdate()
                    .eq(ClearingDetailDO::getId, clearingDetailDO.getId())
                    .eq(ClearingDetailDO::getVersion, clearingDetailDO.getVersion())
                    .set(ClearingDetailDO::getAfterSaleExpireTime, afterSaleExpireTime)
                    .set(ClearingDetailDO::getVersion, clearingDetailDO.getVersion() + 1)
                    .update();
            if (!update) {
                throw new MessageRetryConsumeException("结算单更新失败");
            }
        }


    }


}