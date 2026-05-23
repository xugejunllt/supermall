package com.lanf.seckill.service.strategy;

import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillSuccessMessage;
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
            SecKillSuccessMessage secKillSuccessMessage = new SecKillSuccessMessage();
            secKillSuccessMessage.setSecKillItemId(secKillItemId);
            secKillSuccessMessage.setUserId(userId);
            secKillSuccessMessage.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
            //默认秒杀一个商品
            secKillSuccessMessage.setItemQuantity(1);
            rocketMqClient.sendMessage(
                    SecKillMqTopicName.SEC_KILL_SUCCESS_TOPIC,
                    JsonUtils.toJsonString(secKillSuccessMessage)
            );
            /**
             * 添加秒杀成功 处理中标记
             */
            secKillResultCache.addResult(userId, secKillItemId,
                    SecKillResultEnum.SUCCESS_ORDER_CREATING);
            log.info("发送秒杀成功消息: userId={}, secKillItemId={}, orderNumber={}", 
                    userId, secKillItemId, secKillSuccessMessage.getOrderNumber());
        } catch (Exception e) {
            log.error("秒杀成功,同步订单消息失败: userId={}, secKillItemId={}", 
                    userId, secKillItemId, e);
        }
    }
}
