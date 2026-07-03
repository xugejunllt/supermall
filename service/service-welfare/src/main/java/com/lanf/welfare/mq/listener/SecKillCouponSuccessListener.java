package com.lanf.welfare.mq.listener;

import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import com.lanf.seckill.mq.constant.SecKillClientTopicName;
import com.lanf.welfare.mq.constant.WelfareMqGroupName;
import com.lanf.seckill.mq.message.SecKillCouponSuccessMessage;
import com.lanf.welfare.service.ICouponService;
import com.lanf.welfare.service.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 秒杀优惠券成功监听器
 * 消费优惠券秒杀成功消息，插入用户优惠券
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = SecKillClientTopicName.SEC_KILL_COUPON_SUCCESS_TOPIC,
        consumerGroup = WelfareMqGroupName.SEC_KILL_COUPON_SUCCESS_GROUP
)
public class SecKillCouponSuccessListener implements RocketMQListener<SecKillCouponSuccessMessage> {

    @Autowired
    private ICouponTemplateService couponTemplateService;

    @Autowired
    private ICouponService couponService;
    @Autowired
    private SecKillResultCache secKillResultCache;

    @Transactional
    @Override
    public void onMessage(SecKillCouponSuccessMessage message) {
        log.info("监听到秒杀优惠券成功消息: {}", message);

        Long userId = message.getUserId();
        Long couponTemplateId = message.getCouponTemplateId();

        // 1. 查询优惠券模板
        CouponTemplateDO templateDO = couponTemplateService.getById(couponTemplateId);
        if (templateDO == null) {
            log.error("优惠券模板不存在: couponTemplateId={}", couponTemplateId);
            return;
        }

        // 2. 构建用户优惠券
        CouponDO couponDO = buildCouponDO(templateDO, userId);

        // 3. 扣减优惠券模板剩余数量
        int updateRemainCount = templateDO.getRemainCount() - 1;
        if (updateRemainCount < 0) {
            log.warn("优惠券模板剩余数量不足: couponTemplateId={}", couponTemplateId);
            return;
        }

        boolean update = couponTemplateService.lambdaUpdate()
                .eq(CouponTemplateDO::getId, templateDO.getId())
                .eq(CouponTemplateDO::getVersion, templateDO.getVersion())
                .set(CouponTemplateDO::getRemainCount, updateRemainCount)
                .set(CouponTemplateDO::getVersion, templateDO.getVersion() + 1)
                .update();
        if (!update) {
            log.error("更新优惠券模板剩余数量失败: couponTemplateId={}", couponTemplateId);
            throw new RuntimeException("更新优惠券模板剩余数量失败");
        }

        // 4. 插入用户优惠券
        try {
            couponService.save(couponDO);
        } catch (DuplicateKeyException e) {
            log.warn("用户优惠券已存在: userId={}, couponTemplateId={}", userId, couponTemplateId);
            return;
        }
        secKillResultCache.addResult(userId, message.getSecKillCouponItemId(), SecKillResultEnum.SUCCESS_ORDER_CREATED);
        log.info("秒杀优惠券发放成功: userId={}, couponTemplateId={}", userId, couponTemplateId);
    }

    private CouponDO buildCouponDO(CouponTemplateDO templateDO, Long userId) {
        CouponDO couponDO = new CouponDO();
        couponDO.setCouponTemplateId(templateDO.getId());
        couponDO.setUserId(userId);
        couponDO.setCouponType(templateDO.getScene());
        couponDO.setShopId(templateDO.getShopId());
        couponDO.setName(templateDO.getName());
        couponDO.setTitle(templateDO.getTitle());
        couponDO.setStatus(0); // 待使用
        couponDO.setType(templateDO.getType());
        couponDO.setUseCondition(templateDO.getUseCondition());
        couponDO.setUseStartTime(templateDO.getUseStartTime());
        couponDO.setUseEndTime(templateDO.getUseEndTime());
        couponDO.setCouponTemplateVersion(templateDO.getVersion());
        return couponDO;
    }

}
