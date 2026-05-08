package com.lanf.seckill.mq.listener;

import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.seckill.model.entity.SecKillItemDO;
import com.lanf.seckill.model.entity.SecKillRecordDO;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillMqExecuteMessage;
import com.lanf.seckill.service.ISecKillItemService;
import com.lanf.seckill.service.ISecKillRecordService;
import com.lanf.seckill.service.strategy.AbstractSecKillStrategy;
import com.lanf.seckill.service.strategy.SecKillResultCache;
import com.lanf.seckill.service.strategy.SecKillStrategyFactory;
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

    @Transactional
    @Override
    public void onMessage(SecKillMqExecuteMessage message) {


        SecKillRecordDO one = secKillRecordService.lambdaQuery()
                .eq(SecKillRecordDO::getUserId, message.getUserId())
                .eq(SecKillRecordDO::getSecKillItemId, message.getSecKillItemId())
                .one();

        if (one != null) {
            log.warn("用户已经秒杀成功");
            return;
        }
        SecKillItemDO killItemDO = secKillItemService.getById(message.getSecKillItemId());

        Integer remainingStock = killItemDO.getRemainingStock();
        if (remainingStock <= 0) {
            //写入秒杀结果
            log.warn("库存不足");
            secKillResultCache.addResult(message.getUserId(), message.getSecKillItemId(),
                    SecKillResultEnum.SOLD_OUT);
            return;
        }
        //秒杀成功
        AbstractSecKillStrategy strategy = (AbstractSecKillStrategy) secKillStrategyFactory.
                getStrategy(SeckillModeEnum.MQ_QUEUE.getCode());

        SecKillRecordDO recordDO = new SecKillRecordDO();
        recordDO.setUserId(message.getUserId());
        recordDO.setSecKillItemId(message.getSecKillItemId());
        //默认一次一个商品
        recordDO.setStockQuantity(1);

        try {
            /**
             * 去重 避免重复秒杀
             */
            secKillRecordService.save(recordDO);
        } catch (DuplicateKeyException e) {
            log.warn("用户已经秒杀成功");
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

        strategy.secKillSuccessHandle(message.getUserId(), message.getSecKillItemId());
    }
}
