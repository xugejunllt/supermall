package com.lanf.seckill.service.strategy;

import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.api.SecKillResultCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractSecKillStrategy implements SecKillStrategy {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private SecKillResultCache secKillResultCache;
    /**
     * 秒杀成功处理（发送MQ消息通知订单服务）
     *
     * @param userId 用户ID
     * @param secKillItemId 秒杀商品ID
     */
    public void secKillSuccessHandle(Long userId, Long secKillItemId) {
        try {


        } catch (Exception e) {
            log.error("秒杀成功,同步订单消息失败: userId={}, secKillItemId={}", 
                    userId, secKillItemId, e);
        }
    }
}
