package com.lanf.storage.mq.listener;

import com.lanf.constant.exception.BizException;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.storage.model.entity.StockDO;
import com.lanf.storage.model.entity.StockPreorderPublishLogDO;
import com.lanf.constant.model.enums.storage.PublishStatusEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import com.lanf.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.mq.message.RecycleStockMessage;
import com.lanf.storage.service.stock.IStockPreorderPublishLogService;
import com.lanf.storage.service.stock.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 回收预售库存
 *
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = StorageClientTopicName.RECYCLE_STOCK_TOPIC,
        consumerGroup = StorageMqGroupName.RECYCLE_STOCK_GROUP)
public class RecycleStockListener implements RocketMQListener<RecycleStockMessage> {

    @Autowired
    private IStockPreorderPublishLogService stockPreorderPublishLogService;
    @Autowired
    private IStockService stockService;


    @Transactional
    @Override
    public void onMessage(RecycleStockMessage message) {


        StockDO one = stockService.lambdaQuery()
                .eq(StockDO::getSkuCode, message.getSkuCode())
                .eq(StockDO::getWarehouseId, message.getWarehouseId())
                .one();
        if (one == null){
            log.error("库存不存在");
            throw new BizException("库存不存在");
        }

        Integer usableStock = one.getUsableStock()+message.getChangeQuantity();
        Integer preStock = one.getPreStock()-message.getChangeQuantity();


        StockPreorderPublishLogDO publishLogDO = buildStockPreorderPublishLogDO(message, one);
        try {
            stockPreorderPublishLogService.save(publishLogDO);
        } catch (DuplicateKeyException e) {
           log.warn("该预售库存已存在");
           return;
        }
        boolean update = stockService.lambdaUpdate()
                .eq(StockDO::getId, one.getId())
                .eq(StockDO::getVersion, one.getVersion())
                .set(StockDO::getUsableStock, usableStock)
                .set(StockDO::getPreStock, preStock)
                .set(StockDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新库存失败");
            throw new MessageRetryConsumeException("更新库存失败");
        }

    }

    private StockPreorderPublishLogDO buildStockPreorderPublishLogDO(RecycleStockMessage publishStock, StockDO one){

        StockPreorderPublishLogDO stockPreorderPublishLogDO = new StockPreorderPublishLogDO();
        stockPreorderPublishLogDO.setFlowNo(publishStock.getFlowNo());
        stockPreorderPublishLogDO.setStockId(one.getId());
        stockPreorderPublishLogDO.setSkuCode(publishStock.getSkuCode());
        stockPreorderPublishLogDO.setChangeQuantity(publishStock.getChangeQuantity());
        stockPreorderPublishLogDO.setEventType(StockPreorderEventTypeEnum.RECYCLE);
        stockPreorderPublishLogDO.setPublishPlatform(publishStock.getPublishPlatform());
        stockPreorderPublishLogDO.setWarehouseId(publishStock.getWarehouseId());
        stockPreorderPublishLogDO.setStatus(PublishStatusEnum.SUCCESS);

        return stockPreorderPublishLogDO;
    }


}