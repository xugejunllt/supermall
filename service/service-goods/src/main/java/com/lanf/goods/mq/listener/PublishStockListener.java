package com.lanf.goods.mq.listener;

import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.api.storage.mq.message.PublishStockMessage;
import com.lanf.constant.model.enums.storage.PublishStatusEnum;
import com.lanf.constant.utils.IdUtils;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.entity.UserStockPreorderPublishLogDO;
import com.lanf.goods.mq.constant.GoodsMqGroupName;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.goods.service.stock.IUserStockPreorderPublishLogService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发布预售库存
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = StorageClientTopicName.PUBLISH_STOCK_TOPIC,
        consumerGroup = GoodsMqGroupName.PUBLISH_STOCK_GROUP)
public class PublishStockListener implements RocketMQListener<PublishStockMessage> {

    @Autowired
    private IStockService stockService;

    @Autowired
    private IUserStockFlowService userStockFlowService;
    @Autowired
    private IUserStockPreorderPublishLogService userStockPreorderPublishLogService;
    @Autowired
    private IGoodsSkuService goodsSkuService;


    @Transactional
    @Override
    public void onMessage(PublishStockMessage message) {

        log.info("收到预发售库存消息:{}",message);

        Long goodsId = message.getGoodsId();

        StockDO one = stockService.lambdaQuery()
                .eq(StockDO::getGoodsId, goodsId)
                .eq(StockDO::getSkuCode, message.getSkuCode())
                .eq(StockDO::getWarehouseId, message.getWarehouseId())
                .one();
        boolean saveStockDO = false;
        if (one == null) {
            saveStockDO = true;
            one = new StockDO();
            one.setId(IdUtils.generateId());
            one.setSkuCode(message.getSkuCode());
            one.setUsableStock(message.getChangeQuantity());
            one.setLockStock(0);
            one.setWarehouseId(message.getWarehouseId());
            one.setWarehouseName(message.getWarehouseName());
            one.setTenantId(message.getTenantId());
            one.setAreaCode(message.getAreaCode());
            one.setLatitude(message.getLatitude());
            one.setLongitude(message.getLongitude());
            one.setGoodsId(goodsId);
            one.setVersion(0L);
        }
        Integer usableStock = one.getUsableStock() + message.getChangeQuantity();

        UserStockPreorderPublishLogDO userStockPreorderPublishLogDO = buildStockPreorderPublishLogDO(message, one);

        try {
            userStockPreorderPublishLogService.save(userStockPreorderPublishLogDO);
        } catch (DuplicateKeyException e) {
            /**
             * 消息幂等
             */
            log.warn("重复发布库存，忽略");
            return;
        }
        if (saveStockDO) {
            try {
                stockService.save(one);
            } catch (DuplicateKeyException e) {
                log.warn("库存已存在");
                /**
                 * 重试 走更新库存流程
                 */
                throw new MessageRetryConsumeException("重复发布库存");
            } catch (Exception ignored){

            }
        } else {

            boolean update = stockService.lambdaUpdate()
                    .eq(StockDO::getId, one.getId())
                    .eq(StockDO::getVersion, one.getVersion())
                    .set(StockDO::getUsableStock, usableStock)
                    .set(StockDO::getVersion, one.getVersion() + 1)
                    .update();
            if (!update) {
                log.warn("更新库存失败");
                /**
                 * 重试 走更新库存流程
                 */
                throw new MessageRetryConsumeException("更新库存失败");
            }

        }

    }


    private static UserStockPreorderPublishLogDO buildStockPreorderPublishLogDO(PublishStockMessage message, StockDO one) {
        UserStockPreorderPublishLogDO userStockPreorderPublishLogDO = new UserStockPreorderPublishLogDO();
        userStockPreorderPublishLogDO.setFlowNo(message.getFlowNo());
        userStockPreorderPublishLogDO.setStockId(one.getId());
        userStockPreorderPublishLogDO.setSkuCode(message.getSkuCode());
        userStockPreorderPublishLogDO.setChangeQuantity(message.getChangeQuantity());
        userStockPreorderPublishLogDO.setEventType(message.getEventType());
        userStockPreorderPublishLogDO.setPublishPlatform(message.getPublishPlatform());
        userStockPreorderPublishLogDO.setWarehouseId(message.getWarehouseId());
        userStockPreorderPublishLogDO.setTenantId(message.getTenantId());
        userStockPreorderPublishLogDO.setStatus(PublishStatusEnum.SUCCESS);
        userStockPreorderPublishLogDO.setWarehouseName(message.getWarehouseName());
        userStockPreorderPublishLogDO.setGoodsId(message.getGoodsId());
        return userStockPreorderPublishLogDO;
    }


}