package com.lanf.rocketmq.aspect;

import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.model.enums.MqSendMessageTypeEnum;
import com.lanf.rocketmq.sevice.IMqSendMessageService;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MQ消息发送AOP切面
 * <p>扫描Spring事务注解，在事务提交成功后，从内存队列中获取消息并发送到消息队列</p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Slf4j
@Aspect
@Component
public class MqSendMessageAop {

    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private IMqSendMessageService mqSendMessageService;

    /**
     * 拦截带有@Transactional注解的方法
     *
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSendMessages();
                }

                @Override
                public void afterCompletion(int status) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        MqSendMessageUtils.clear();
                    }
                }
            });
        }

        return joinPoint.proceed();
    }

    /**
     * 从内存队列中获取消息并发送到MQ
     */
    private void doSendMessages() {
        List<MqSendMessageDO> messages = MqSendMessageUtils.getAndClearMessages();
        if (messages.isEmpty()) {
            return;
        }

        log.info("事务提交成功，开始发送MQ消息，共{}条", messages.size());

        for (MqSendMessageDO messageDO : messages) {
            sendMessage(messageDO);
        }
    }

    /**
     * 根据消息类型发送到对应的MQ
     *
     * @param messageDO 消息记录
     */
    private void sendMessage(MqSendMessageDO messageDO) {
        MqSendMessageTypeEnum type = messageDO.getSendMessageType();
        if (type == null) {
            log.warn("消息类型为空，跳过发送，messageId:{}", messageDO.getId());
            return;
        }

        try {
            switch (type) {
                case NORMAL:
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    break;
                case ORDERED:
                    if (messageDO.getMessageKey() == null) {
                        log.warn("顺序消息缺少messageKey，降级为普通消息发送，messageId:{}", messageDO.getId());
                        rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    } else {
                        rocketMqClient.syncSendOrderly(messageDO.getTopic(), messageDO.getMessageContent(), messageDO.getMessageKey());
                    }
                    break;
                case NORMAL_TAG:
                    if (messageDO.getTag() == null) {
                        log.warn("带Tag消息缺少tag，降级为普通消息发送，messageId:{}", messageDO.getId());
                        rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    } else {
                        rocketMqClient.sendMessageWithTags(messageDO.getTopic(), messageDO.getTag(), messageDO.getMessageContent());
                    }
                    break;
                case ORDERED_TAG:
                    if (messageDO.getTag() == null || messageDO.getMessageKey() == null) {
                        log.warn("顺序带Tag消息缺少tag或messageKey，降级为普通消息发送，messageId:{}", messageDO.getId());
                        rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    } else {
                        rocketMqClient.sendOrderlyMessageWithTags(messageDO.getTopic(), messageDO.getTag(), messageDO.getMessageContent(), messageDO.getMessageKey());
                    }
                    break;
                case DELAY_NORMAL:
                    if (messageDO.getDelayTime() == null) {
                        log.warn("延迟消息缺少delayTime，降级为普通消息发送，messageId:{}", messageDO.getId());
                        rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    } else {
                        rocketMqClient.sendDelayMessage(messageDO.getTopic(), messageDO.getMessageContent(), TimeUnit.MINUTES, messageDO.getDelayTime());
                    }
                    break;
                case DELAY_ORDERED:
                    if (messageDO.getDelayTime() == null || messageDO.getMessageKey() == null) {
                        log.warn("延迟顺序消息缺少delayTime或messageKey，降级为普通消息发送，messageId:{}", messageDO.getId());
                        rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    } else {
                        String delayContent = messageDO.getMessageContent();
                        rocketMqClient.sendDelayMessage(messageDO.getTopic(), delayContent, TimeUnit.MINUTES, messageDO.getDelayTime());
                    }
                    break;
                case DELAY_NORMAL_TAG:
                    if (messageDO.getDelayTime() == null || messageDO.getTag() == null) {
                        log.warn("延迟带Tag消息缺少delayTime或tag，降级为普通消息发送，messageId:{}", messageDO.getId());
                        rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    } else {
                        String delayContent = messageDO.getMessageContent();
                        rocketMqClient.sendDelayMessage(messageDO.getTopic() + ":" + messageDO.getTag(), delayContent, TimeUnit.MINUTES, messageDO.getDelayTime());
                    }
                    break;
                case DELAY_ORDERED_TAG:
                    if (messageDO.getDelayTime() == null || messageDO.getTag() == null || messageDO.getMessageKey() == null) {
                        log.warn("延迟顺序带Tag消息缺少必要参数，降级为普通消息发送，messageId:{}", messageDO.getId());
                        rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    } else {
                        String delayContent = messageDO.getMessageContent();
                        rocketMqClient.sendDelayMessage(messageDO.getTopic() + ":" + messageDO.getTag(), delayContent, TimeUnit.MINUTES, messageDO.getDelayTime());
                    }
                    break;
                default:
                    log.warn("未知的消息类型:{}, 降级为普通消息发送，messageId:{}", type, messageDO.getId());
                    rocketMqClient.sendMessage(messageDO.getTopic(), messageDO.getMessageContent());
                    break;
            }

            updateMessageStatus(messageDO);
            log.info("MQ消息发送成功，messageId:{}, topic:{}, type:{}", messageDO.getId(), messageDO.getTopic(), type.getName());

        } catch (Exception e) {
            log.error("MQ消息发送失败，不更新状态，messageId:{}, topic:{}, type:{}", messageDO.getId(), messageDO.getTopic(), type != null ? type.getName() : null, e);
        }
    }

    /**
     * 更新消息状态为发送成功
     *
     * @param messageDO 消息记录
     */
    private void updateMessageStatus(MqSendMessageDO messageDO) {
        try {
            messageDO.setStatus(1);
            mqSendMessageService.updateById(messageDO);
        } catch (Exception e) {
            log.error("更新MQ消息状态失败，messageId:{}", messageDO.getId(), e);
        }
    }
}
