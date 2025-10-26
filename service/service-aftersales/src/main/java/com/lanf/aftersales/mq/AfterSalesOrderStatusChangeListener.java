package com.lanf.aftersales.mq;

import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.AfterSalesOrderStatusChangeDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RocketMQMessageListener(topic = TopicName.AFTER_SALES_ORDER_STATUS_CHANGE_TOPIC, consumerGroup = TopicName.AFTER_SALES_ORDER_STATUS_CHANGE_GROUP)
public class AfterSalesOrderStatusChangeListener implements RocketMQListener<String> {

    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;

    /**
     * 监听售后单状态变更
     */
    @Override
    public void onMessage(String message) {

        log.info("监听售后单状态变更mq消息:{}", message);
        AfterSalesOrderStatusChangeDTO dto = JsonUtils.toObject(message, AfterSalesOrderStatusChangeDTO.class);
        List<Long> afterSalesOrderIdList = dto.getAfterSalesOrderIdList();
        Integer event = dto.getEvent();
        if (event == 0) {
            afterSalesOrderService.exchangeGoodsOutStockFinish(afterSalesOrderIdList.get(0));
        }

    }
}