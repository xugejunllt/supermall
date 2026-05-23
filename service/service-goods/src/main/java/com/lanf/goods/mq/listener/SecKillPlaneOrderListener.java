package com.lanf.goods.mq.listener;

import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.goods.UserStockFlowEventTypeEnum;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.entity.UserStockFlowDO;
import com.lanf.goods.mq.constant.GoodsMqGroupName;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.welfare.mq.constant.SecKillClientTopicName;
import com.lanf.welfare.mq.message.SecKillPlaneMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * 秒杀成功 扣减库存
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = SecKillClientTopicName.SEC_KILL_PLANE_TOPIC,
        consumerGroup = GoodsMqGroupName.DEDUCT_FROZEN_STOCK_GROUP)
public class SecKillPlaneOrderListener implements RocketMQListener<SecKillPlaneMessage> {

    @Autowired
    private IStockService stockService;

    @Autowired
    private IUserStockFlowService userStockFlowService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(SecKillPlaneMessage message) {

        log.info("秒杀成功,扣减库存开始");
        StockDO stockDO = stockService.lambdaQuery()
                .eq(StockDO::getGoodsId, message.getGoodsId())
                .eq(StockDO::getSkuCode, message.getSkuCode())
                .eq(StockDO::getWarehouseId, message.getWarehouseId())
                .one();
        if (stockDO == null) {
            log.error("秒杀订单创建成功，但是库存不存在");
            throw new BizException("秒杀订单创建成功，但是库存不存在");

        }
        if (stockDO.getUsableStock() < message.getQuantity()) {
            log.error("秒杀订单创建成功，但是库存不足");
            /**
             * 发送通知 标记订单为异常
             */


            return;
        }

        String flowNo = message.getGoodsId() + "_" +
                message.getSkuCode() + ":" + message.getOrderNumber() + ":" +
                UserStockFlowEventTypeEnum.ORDER_OUTBOUND.getCode();

        Integer beforeQuantity = stockDO.getUsableStock() + stockDO.getLockStock();
        Integer afterQuantity = beforeQuantity - message.getQuantity();
        Integer updateUsableStock = stockDO.getUsableStock() - message.getQuantity();
        UserStockFlowDO userStockFlowDO = new UserStockFlowDO();
        userStockFlowDO.setGoodsId(message.getGoodsId());
        userStockFlowDO.setFlowNo(flowNo);
        userStockFlowDO.setUserStockId(stockDO.getId());
        userStockFlowDO.setSkuCode(message.getSkuCode());
        userStockFlowDO.setWarehouseId(message.getWarehouseId());
        userStockFlowDO.setOrderId(message.getOrderId());
        userStockFlowDO.setEventType(UserStockFlowEventTypeEnum.ORDER_OUTBOUND);
        userStockFlowDO.setBeforeQuantity(beforeQuantity);
        userStockFlowDO.setChangeQuantity(message.getQuantity());
        userStockFlowDO.setAfterQuantity(afterQuantity);
        userStockFlowDO.setTenantId(stockDO.getTenantId());
        try {
            userStockFlowService.save(userStockFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("库存流水已添加");
            return;
        }
        boolean update = stockService.lambdaUpdate()
                .set(StockDO::getUsableStock, updateUsableStock)
                .set(StockDO::getVersion, stockDO.getVersion()+1)
                .eq(StockDO::getId, stockDO.getId())
                .eq(StockDO::getVersion, stockDO.getVersion())
                .update();

        if (!update) {
            log.warn("库存更新失败");
            throw new MessageRetryConsumeException("库存更新失败");
        }


    }


}