package com.lanf.seckill.mq.listener;

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import com.lanf.seckill.api.SecKillResultCache;
import com.lanf.seckill.model.entity.SecKillCouponItemDO;
import com.lanf.seckill.model.entity.SecKillCouponRecordDO;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import com.lanf.seckill.mq.constant.SecKillClientTopicName;
import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillCouponMqExecuteMessage;
import com.lanf.seckill.mq.message.SecKillCouponSuccessMessage;
import com.lanf.seckill.service.ISecKillCouponItemService;
import com.lanf.seckill.service.ISecKillCouponRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 秒杀优惠券执行监听器
 * 消费秒杀优惠券执行消息，插入秒杀记录，发送优惠券发放消息
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = SecKillMqTopicName.SEC_KILL_COUPON_MQ_EXECUTE_TOPIC,
        consumerGroup = SecKillMqGroupName.SEC_KILL_COUPON_MQ_EXECUTE_GROUP
)
public class SecKillCouponMqExecuteListener implements RocketMQListener<SecKillCouponMqExecuteMessage> {

    @Autowired
    private ISecKillCouponItemService seckillCouponItemService;

    @Autowired
    private ISecKillCouponRecordService seckillCouponRecordService;

    @Autowired
    private SecKillResultCache secKillResultCache;

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @Transactional
    @Override
    public void onMessage(SecKillCouponMqExecuteMessage message) {
        log.info("监听到秒杀优惠券消息: {}", message);

        Long userId = message.getUserId();
        Long secKillCouponItemId = message.getSecKillCouponItemId();

        // 1. 查询秒杀优惠券信息
        SecKillCouponItemDO couponItemDO = seckillCouponItemService.getById(secKillCouponItemId);
        if (couponItemDO == null) {
            log.error("秒杀优惠券不存在: secKillCouponItemId={}", secKillCouponItemId);
            return;
        }

        // 2. 构建秒杀记录实体，用于幂等校验
        SecKillCouponRecordDO recordDO = new SecKillCouponRecordDO();
        recordDO.setUserId(userId);
        recordDO.setSecKillCouponItemId(secKillCouponItemId);
        recordDO.setStockQuantity(1);
        recordDO.setCouponTemplateId(couponItemDO.getCouponTemplateId());
        recordDO.setTenantId(couponItemDO.getTenantId());
        recordDO.setStatus(0); // 秒杀成功

        // 3. 幂等去重：通过数据库唯一索引(user_id + sec_kill_coupon_item_id)防止重复秒杀
        try {
            seckillCouponRecordService.save(recordDO);
        } catch (DuplicateKeyException e) {
            log.warn("用户已经秒杀成功: userId={}, secKillCouponItemId={}", userId, secKillCouponItemId);
            return;
        }

        // 4. MQ_QUEUE模式：使用乐观锁扣减秒杀优惠券库存
        if (SeckillModeEnum.MQ_QUEUE.equals(message.getSeckillModeEnum())) {
            Integer remainingStock = couponItemDO.getRemainingStock();
            if (remainingStock <= 0) {
                log.error("库存不足: secKillCouponItemId={}", secKillCouponItemId);
                secKillResultCache.addResult(userId, secKillCouponItemId, SecKillResultEnum.SOLD_OUT);
                throw new BizException("库存不足");
            }
            boolean updated = seckillCouponItemService.lambdaUpdate()
                    .eq(SecKillCouponItemDO::getId, secKillCouponItemId)
                    .eq(SecKillCouponItemDO::getVersion, couponItemDO.getVersion())
                    .set(SecKillCouponItemDO::getRemainingStock, remainingStock - 1)
                    .set(SecKillCouponItemDO::getVersion, couponItemDO.getVersion() + 1)
                    .update();
            if (!updated) {
                log.warn("更新秒杀优惠券失败: secKillCouponItemId={}", secKillCouponItemId);
                throw new com.lanf.rocketmq.exception.MessageRetryConsumeException("更新秒杀优惠券失败");
            }
        }

        // 5. 构建优惠券发放消息
        SecKillCouponSuccessMessage couponSuccessMessage = buildSecKillCouponSuccessMessage(couponItemDO, userId);

        // 6. 发送优惠券发放消息
        mqSendMessageUtils.sendMessage(SecKillClientTopicName.SEC_KILL_COUPON_SUCCESS_TOPIC,
                JsonUtils.toJsonString(couponSuccessMessage),null);

        // 7. 标记秒杀结果为"优惠券发放中"
        secKillResultCache.addResult(userId, secKillCouponItemId,
                SecKillResultEnum.SUCCESS_ORDER_CREATING);
    }

    private SecKillCouponSuccessMessage buildSecKillCouponSuccessMessage(SecKillCouponItemDO couponItemDO, Long userId) {
        SecKillCouponSuccessMessage message = new SecKillCouponSuccessMessage();
        message.setUserId(userId);
        message.setSecKillCouponItemId(couponItemDO.getId());
        message.setCouponTemplateId(couponItemDO.getCouponTemplateId());
        return message;
    }

}
