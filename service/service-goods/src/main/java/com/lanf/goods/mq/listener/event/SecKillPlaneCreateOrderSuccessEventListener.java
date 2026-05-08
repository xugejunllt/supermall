package com.lanf.goods.mq.listener.event;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.entity.UserStockFlowDO;
import com.lanf.goods.model.enums.UserStockFlowEventTypeEnum;
import com.lanf.goods.mq.constant.GoodsMqGroupName;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.order.model.enums.OrderProcessStepEnum;
import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.message.SecKillOrderConfirmMessage;
import com.lanf.order.mq.message.SecKillPlaneCreateOrderSuccessMessage;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 秒杀 订单创建成功之后 ，创建交易单
 *
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = OrderClientTopicName.SEC_KILL_PLANE_CREATE_ORDER_SUCCESS_EVENT_TOPIC,
        consumerGroup = GoodsMqGroupName.DEDUCT_FROZEN_STOCK_GROUP
)
public class SecKillPlaneCreateOrderSuccessEventListener implements RocketMQListener<SecKillPlaneCreateOrderSuccessMessage> {

    @Autowired
    private IStockService stockService;

    @Autowired
    private IUserStockFlowService userStockFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void onMessage(SecKillPlaneCreateOrderSuccessMessage message) {


        StockDO stockDO = stockService.lambdaQuery()
                .eq(StockDO::getSkuCode, message.getSkuCode())
                .eq(StockDO::getWarehouseId, message.getWarehouseId())
                .one();
        if (stockDO == null){
            log.error("秒杀订单创建成功，但是库存不存在");
            throw new BizException("秒杀订单创建成功，但是库存不存在");

        }
        if (stockDO.getUsableStock() < message.getQuantity()){
            log.error("秒杀订单创建成功，但是库存不足");
            /**
             * 发送通知
             */
            SecKillOrderConfirmMessage message1 = new SecKillOrderConfirmMessage();
            message1.setOrderNumber(message.getOrderNumber());
            message1.setOrderProcessStep(OrderProcessStepEnum.STOCK_DEDUCT_FAILED);
            rocketMqClient.sendMessage(OrderClientTopicName.SEC_KILL_ORDER_CONFIRM_TOPIC,
                    JsonUtils.toJsonString(message1));
            return;
        }
        Integer beforeQuantity = stockDO.getUsableStock()+stockDO.getLockStock();
        Integer afterQuantity = beforeQuantity - message.getQuantity();
        Integer updateLockStock = stockDO.getLockStock()-message.getQuantity();
        UserStockFlowDO userStockFlowDO = new UserStockFlowDO();
        userStockFlowDO.setFlowNo(message.getStockFlowNo());
        userStockFlowDO.setUserStockId(stockDO.getId());
        userStockFlowDO.setSkuCode(message.getSkuCode());
        userStockFlowDO.setWarehouseId(message.getWarehouseId());
        userStockFlowDO.setOrderId(message.getOrderId());
        userStockFlowDO.setEventType(UserStockFlowEventTypeEnum.ORDER_OUTBOUND);
        userStockFlowDO.setBeforeQuantity(beforeQuantity);
        userStockFlowDO.setChangeQuantity(message.getQuantity());
        userStockFlowDO.setAfterQuantity(afterQuantity);
        try {
            userStockFlowService.save(userStockFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("库存流水已添加");
            return;
        }
        boolean update = stockService.lambdaUpdate()
                .set(StockDO::getLockStock, updateLockStock)
                .set(StockDO::getVersion, stockDO.getVersion())
                .eq(StockDO::getId, stockDO.getId())
                .eq(StockDO::getVersion, stockDO.getVersion())
                .update();
        if (!update){
            log.warn("库存更新失败");
            throw new MessageRetryConsumeException("库存更新失败");
        }
        /**
         * 发送通知
         */
        SecKillOrderConfirmMessage message1 = new SecKillOrderConfirmMessage();
        message1.setOrderNumber(message.getOrderNumber());
        message1.setOrderProcessStep(OrderProcessStepEnum.STOCK_DEDUCTED);
        rocketMqClient.sendMessage(OrderClientTopicName.SEC_KILL_ORDER_CONFIRM_TOPIC,
                JsonUtils.toJsonString(message1));
    }
}
