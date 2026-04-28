package com.lanf.finance.mq.listener;

import com.lanf.aftersales.api.AfterSalesOrderApiService;
import com.lanf.aftersales.model.dto.UnderAfterSaleDTO;
import com.lanf.constant.result.Result;
import com.lanf.finance.model.entity.ClearingOrderDO;
import com.lanf.finance.model.enums.ClearingOrderStatusEnum;
import com.lanf.finance.mq.constant.FinanceMqGroupName;
import com.lanf.finance.mq.constant.FinanceMqTopicName;
import com.lanf.finance.mq.message.ClearingOrderMessage;
import com.lanf.finance.service.IClearingOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = FinanceMqTopicName.SETTLEMENT_TASK_TOPIC,
        consumerGroup = FinanceMqGroupName.SETTLEMENT_TASK_GROUP
)
public class ClearingOrderListener implements RocketMQListener<ClearingOrderMessage> {

    @Autowired
    private IClearingOrderService liquidationService;

    @Autowired
    private AfterSalesOrderApiService afterSalesOrderApiService;

    @Override
    public void onMessage(ClearingOrderMessage message) {
        log.info("收到结算任务消息: {}", message);

        Long liquidationId = message.getLiquidationId();
        Long orderId = message.getOrderId();

        ClearingOrderDO liquidation = liquidationService.getById(liquidationId);
        if (liquidation == null) {
            log.error("清算单不存在: {}", liquidationId);
            return;
        }

        if (!ClearingOrderStatusEnum.WAIT_SETTLEMENT.equals(liquidation.getStatus())) {
            log.warn("清算单状态不是待结算: {}", liquidationId);
            return;
        }

        UnderAfterSaleDTO underAfterSaleDTO = new UnderAfterSaleDTO();
        underAfterSaleDTO.setOrderId(orderId);
        Result<Boolean> result = afterSalesOrderApiService.isUnderAfterSale(underAfterSaleDTO);

        if (result != null && Boolean.TRUE.equals(result.getData())) {
            log.info("订单 {} 正在售后中，暂不结算", orderId);
            return;
        }

        boolean updated = liquidationService.lambdaUpdate()
                .eq(ClearingOrderDO::getId, liquidationId)
                .eq(ClearingOrderDO::getStatus, ClearingOrderStatusEnum.WAIT_SETTLEMENT)
                .set(ClearingOrderDO::getStatus, ClearingOrderStatusEnum.SETTLED)
                .update();

        if (updated) {
            log.info("清算单 {} 结算成功", liquidationId);
        } else {
            log.warn("清算单 {} 结算失败，可能已被处理", liquidationId);
        }


    }
}
