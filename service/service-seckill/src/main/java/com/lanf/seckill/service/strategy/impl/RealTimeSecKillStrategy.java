package com.lanf.seckill.service.strategy.impl;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.model.dto.PlaceDTO;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillSuccessMessage;
import com.lanf.seckill.service.strategy.SecKillStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.lanf.seckill.service.impl.SecKillActivityServiceImpl.SECKILL_ITEM_STOCK_KEY_PRX;

@Slf4j
@Component
 class RealTimeSecKillStrategy implements SecKillStrategy {

    @Autowired
    private RedissonCacheService redissonCacheService;
    @Autowired
    private RocketMqClient rocketMqClient;


    public static final String USER_PARTICIPATED_KEY_PRX = "seckill:user:participated:%s:%s";

    @Override
    public void executeSecKill(PlaceDTO dto) {
        Long secKillItemId = dto.getSeckillItemId();
        Long userId = dto.getUserId();

        String stockKey = String.format(SECKILL_ITEM_STOCK_KEY_PRX, secKillItemId);

        // 检查用户是否已经参与过该商品的秒杀（使用 Redis 递增）
        String participatedKey = String.format(USER_PARTICIPATED_KEY_PRX, userId, secKillItemId);
        long participateCount = redissonCacheService.incrementAndGet(participatedKey, 1, TimeUnit.DAYS);
        // 如果计数大于1，说明用户已经参与过
        if (participateCount > 1) {
            throw new BizException("您已经参与过该商品秒杀");

        }
        //1.扣减库存
        long decremented = redissonCacheService.decrementAndGet(stockKey);
        if (decremented >= 0) {
            /**
             * 秒杀成功
             */
            log.info("秒杀成功userId={},secKillItemId={}",userId,secKillItemId);
            secKillSuccessHandle(userId, secKillItemId);
        } else if (decremented == -1 || participateCount == -1) {
            /**
             * redis 异常 允许用户重试
             */
            redissonCacheService.delete(stockKey);
            throw new BizException("太火爆了，再试一次");

        } else {
            throw new BizException("商品已售罄");
        }
    }
    private void secKillSuccessHandle(Long userId, Long secKillItemId) {
        //秒杀成功
        try {
            SecKillSuccessMessage secKillSuccessMessage = new SecKillSuccessMessage();
            secKillSuccessMessage.setSecKillItemId(secKillItemId);
            secKillSuccessMessage.setUserId(userId);
            secKillSuccessMessage.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
            rocketMqClient.sendMessage( SecKillMqTopicName.SEC_KILL_SUCCESS_TOPIC,
                    JsonUtils.toJsonString(secKillSuccessMessage));
        } catch (Exception e) {
            //打印erro 人工处理
            log.error("秒杀成功,同步订单消息失败: userId={}, seckillItemId={}",
                    userId, secKillItemId, e);

        }
    }
    @Override
    public Integer getSupportedMode() {
        return SeckillModeEnum.REAL_TIME.getCode();
    }
}
