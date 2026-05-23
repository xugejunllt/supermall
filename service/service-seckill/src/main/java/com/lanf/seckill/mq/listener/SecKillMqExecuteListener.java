package com.lanf.seckill.mq.listener;

import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.utils.IdUtils;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.model.entity.SecKillItemDO;
import com.lanf.seckill.model.entity.SecKillRecordDO;
import com.lanf.welfare.model.enums.SecKillResultEnum;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillMqExecuteMessage;
import com.lanf.seckill.service.ISecKillItemService;
import com.lanf.seckill.service.ISecKillRecordService;
import com.lanf.welfare.api.SecKillResultCache;
import com.lanf.seckill.service.strategy.SecKillStrategyFactory;
import com.lanf.welfare.mq.constant.SecKillClientTopicName;
import com.lanf.welfare.mq.message.SecKillPlaneMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = SecKillMqTopicName.SEC_KILL_MQ_EXECUTE_TOPIC,
        consumerGroup = SecKillMqGroupName.SEC_KILL_MQ_EXECUTE_GROUP
)
public class SecKillMqExecuteListener implements RocketMQListener<SecKillMqExecuteMessage> {

    @Autowired
    private ISecKillItemService secKillItemService;
    @Autowired
    private SecKillStrategyFactory secKillStrategyFactory;
    @Autowired
    private SecKillResultCache secKillResultCache;
    @Autowired
    private ISecKillRecordService secKillRecordService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Transactional
    @Override
    public void onMessage(SecKillMqExecuteMessage message) {

        log.info("监听到秒杀成功消息,生成秒杀记录:{}",message);

        SecKillItemDO killItemDO = secKillItemService.getById(message.getSecKillItemId());

        //默认一次一个商品
        Integer stockQuantity = 1;
        Long orderId = IdUtils.generateId();
        SecKillRecordDO recordDO = new SecKillRecordDO();
        recordDO.setUserId(message.getUserId());
        recordDO.setSecKillItemId(message.getSecKillItemId());
        recordDO.setStockQuantity(stockQuantity);
        recordDO.setTenantId(killItemDO.getTenantId());
        recordDO.setOrderId(orderId);
        SecKillPlaneMessage secKillPlaneMessage = buildSecKillPlaneMessage(killItemDO, message.getUserId(),
                stockQuantity,orderId);

        try {
            /**
             * 去重 避免重复秒杀
             */
            secKillRecordService.save(recordDO);
        } catch (DuplicateKeyException e) {
            log.warn("用户已经秒杀成功");
            return;
        }
        if (SeckillModeEnum.MQ_QUEUE.equals(message.getSeckillModeEnum())){
            /**
             * 消息队列秒杀模式 保证不超卖
             */
            log.info("消息队列秒杀模式");
            Integer remainingStock = killItemDO.getRemainingStock();
            if (remainingStock <= 0) {
                //写入秒杀结果
                log.error("库存不足");
                secKillResultCache.addResult(message.getUserId(), message.getSecKillItemId(),
                        SecKillResultEnum.SOLD_OUT);
                return;
            }
            boolean updated = secKillItemService.lambdaUpdate()
                    .eq(SecKillItemDO::getId, message.getSecKillItemId())
                    .eq(SecKillItemDO::getVersion, killItemDO.getVersion())
                    .set(SecKillItemDO::getRemainingStock, remainingStock - 1)
                    .set(SecKillItemDO::getVersion, killItemDO.getVersion() + 1)
                    .update();
            if (!updated) {
                log.warn("更新秒杀商品失败,秒杀失败");
                secKillResultCache.addResult(message.getUserId(), message.getSecKillItemId(),
                        SecKillResultEnum.SOLD_OUT);
                /**
                 * 进行重试 3次
                 */
                throw new MessageRetryConsumeException("更新秒杀商品失败,秒杀失败");
            }

        } else {
            log.info("实时秒杀模式");
        }
        rocketMqClient.sendMessage(SecKillClientTopicName.SEC_KILL_PLANE_TOPIC,
                JsonUtils.toJsonString(secKillPlaneMessage));
        /**
         * 添加秒杀成功 处理中标记
         */
        secKillResultCache.addResult(message.getUserId(), message.getSecKillItemId(),
                SecKillResultEnum.SUCCESS_ORDER_CREATING);
    }

    private SecKillPlaneMessage buildSecKillPlaneMessage(SecKillItemDO killItemDO ,Long userId,
                                                         Integer stockQuantity,Long orderId){
        SecKillPlaneMessage secKillPlaneMessage = new SecKillPlaneMessage();
        secKillPlaneMessage.setShopId(killItemDO.getShopId());
        secKillPlaneMessage.setShopName(killItemDO.getShopName());
        secKillPlaneMessage.setTenantId(killItemDO.getTenantId());
        secKillPlaneMessage.setUserId(userId);
        secKillPlaneMessage.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
        //取默认收货地址
        secKillPlaneMessage.setTakeAddress(null);
        //秒杀商品 默认无售后
        secKillPlaneMessage.setAfterSaleDays(0);
        secKillPlaneMessage.setGoodsId(killItemDO.getItemId());
        secKillPlaneMessage.setGoodsName(killItemDO.getGoodsName());
        secKillPlaneMessage.setGoodsTitle(killItemDO.getItemTitle());
        secKillPlaneMessage.setSkuId(killItemDO.getSkuId());
        secKillPlaneMessage.setSkuName(killItemDO.getAttributes());
        secKillPlaneMessage.setSkuPictureAddress(killItemDO.getSkuCode());
        secKillPlaneMessage.setQuantity(stockQuantity);
        secKillPlaneMessage.setWarehouseId(killItemDO.getWarehouseId());
        secKillPlaneMessage.setGoodsVersion(killItemDO.getGoodsVersion());
        secKillPlaneMessage.setSkuVersion(killItemDO.getSkuVersion());
        secKillPlaneMessage.setSkuCode(killItemDO.getSkuCode());
        secKillPlaneMessage.setUnitPrice(killItemDO.getSeckillPrice());
        secKillPlaneMessage.setOrderId(orderId);
        return  secKillPlaneMessage;
    }
}
