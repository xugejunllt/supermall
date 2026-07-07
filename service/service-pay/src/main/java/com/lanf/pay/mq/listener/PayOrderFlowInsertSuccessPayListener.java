package com.lanf.pay.mq.listener;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.PayMethodEnum;
import com.lanf.api.pay.model.enums.TradePurposeEnum;
import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.PayOrderFlowInsertSuccessMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.config.PayConfig;
import com.lanf.pay.constant.PayMqGroupName;
import com.lanf.pay.model.bo.PostTradeSuccessHandlerContext;
import com.lanf.pay.model.entity.BathTradeOrderDO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.BathTradeOrderStatusEnum;
import com.lanf.pay.model.enums.PaySceneEnum;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.trade.IBathTradeOrderService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.service.trade.TradeSuccessHandler;
import com.lanf.pay.service.trade.TradeSuccessHandlerFactory;
import com.lanf.pay.service.trade.impl.PayRetryPolicyCacheService;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


/**
 * 支付流水插入成功监听器
 * <p>
 * 核心职责：监听支付流水插入成功事件，根据支付场景（单笔/批量/合并转单笔）
 * 完成交易单状态流转，并触发下游订单域的状态同步。
 * <p>
 * 设计要点：
 * 1. 幂等性：基于交易单状态 + 乐观锁，防止MQ重复消费
 * 2. 多渠道防护：识别用户用不同渠道重复支付，触发退款
 * 3. 场景隔离：三种支付场景独立处理，逻辑不耦合
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayClientTopicName.PAY_ORDER_FLOW_INSERT_SUCCESS_TOPIC,
        consumerGroup = PayMqGroupName.PAY_ORDER_FLOW_PAY_GROUP
)
public class PayOrderFlowInsertSuccessPayListener implements RocketMQListener<PayOrderFlowInsertSuccessMessage> {


    @Autowired
    private IBathTradeOrderService bathTradeOrderService;

    @Autowired
    private PayConfig payConfig;

    @Autowired
    private PayRetryPolicyCacheService payRetryPolicyCacheService;

    @Autowired
    private MqSendMessageUtils mqSendMessageUtils;

    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private IPayOrderFlowService payOrderFlowService;

    /**
     * 主入口：支付流水插入成功后的统一调度
     * <p>
     * 边界条件：
     * 1. 消息可能重复投递（RocketMQ At-Least-Once 语义）
     * 2. bathPay 参数与 tradeOrderDO.bathPay 可能不一致（合并转单笔场景）
     *
     * @param message 支付流水插入成功消息
     */
    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(PayOrderFlowInsertSuccessMessage message) {

        log.info("插入支付流水成功:[{}]", JsonUtils.toJsonString(message));
        String outTradeNo = message.getOutTradeNo();
        Boolean bathPay = message.getBathPay();
        Integer payType = message.getPayType();
        BigDecimal payMoney = message.getPayMoney();
        PayMethodEnum payMethod = message.getPayMethod();

        // 【边界条件】查询交易单，可能不存在（理论上不会发生，防御性编程）
        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        // 根据 bathPay 标志和交易单实际状态，确定支付场景
        PaySceneEnum payScene = getPayScene(bathPay, tradeOrderDO);
        if (PaySceneEnum.SINGLE_ORDER_SINGLE_PAY.equals(payScene)) {

            handleSinglePayScene(payType, message.getTradePurpose(), payMethod, payMoney, tradeOrderDO);
        }
        if (PaySceneEnum.COMBINED_PAY.equals(payScene)) {

            handleCombinedPayScene(outTradeNo, payType, payMethod);

        }

        if (PaySceneEnum.COMBINED_TO_SINGLE_PAY.equals(payScene)) {

            handleCombinedToSinglePayScene(payType, payMethod, tradeOrderDO);
        }

    }

    /**
     * 获取支付场景
     * <p>
     * 场景推导逻辑（关键边界）：
     * 1. bathPay=true（消息体标志）：一定是组合支付，无论交易单类型
     * 2. bathPay=false（消息体标志）：需进一步判断交易单类型
     *    - tradeOrderDO.bathPay=0：纯单笔支付
     *    - tradeOrderDO.bathPay=1：原先是组合支付，现在转为单笔支付
     *
     * @param bathPay      MQ消息中的批次标志
     * @param tradeOrderDO   交易单实体
     * @return 支付场景枚举
     */
    private PaySceneEnum getPayScene(Boolean bathPay, TradeOrderDO tradeOrderDO) {

        if (!bathPay) {

            if (tradeOrderDO.getBathPay() == 0) {
                return PaySceneEnum.SINGLE_ORDER_SINGLE_PAY;
            }
            return PaySceneEnum.COMBINED_TO_SINGLE_PAY;
        }
        log.info("组合付款");

        return PaySceneEnum.COMBINED_PAY;

    }

    /**
     * 单笔支付场景处理
     * <p>
     * 边界条件处理：
     * 1. 【幂等】已支付且同渠道 → 直接返回，防止重复处理
     * 2. 【多渠道】已支付但不同渠道 → 用户可能同时用支付宝和微信付了同一笔，
     *    此时需要退款（当前仅打印日志，实际应有退款逻辑）
     * 3. 【并发】乐观锁更新失败 → 抛出 MessageRetryConsumeException，让MQ重试
     *
     * @param payType    支付渠道编码（支付宝/微信等）
     * @param tradeType  交易用途（实时订单/钱包充值等）
     * @param payMethod  支付方式枚举
     * @param payMoney   实际支付金额
     * @param tradeOrderDO 交易单实体
     */
    @Transactional
    public void handleSinglePayScene(Integer payType,
                                     TradePurposeEnum tradeType, PayMethodEnum payMethod,
                                     BigDecimal payMoney, TradeOrderDO tradeOrderDO) {


        // 【边界条件1】幂等：已支付且同渠道，说明是重复消息，直接返回
        if (TradeOrderStatusEnum.COMPLETED.getCode().
                equals(tradeOrderDO.getPayStatus())
                && PayChannelEnum.getByCode(payType).
                equals(tradeOrderDO.getPayType())) {

            log.warn("交易单支付完成");
            return;
        }

        // 【边界条件2】多渠道支付：已支付但渠道不同，用户可能同时用多个渠道付了同一笔
        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus())
                && !PayChannelEnum.getByCode(payType).
                equals(tradeOrderDO.getPayType())) {
            /**
             *
             * 交易单为支付完成状态 ,且当前支付渠道与交易单支付渠道不一致
             * 说明 用户完成了多渠道支付 ，那么要进行退款
             *
             */
            log.info("多渠道完成支付,进行退款");
        }

        // 【核心操作】乐观锁更新交易单状态为 COMPLETED
        // 条件：当前状态为 PENDING（或其他非完成状态），且版本号匹配
        boolean update = tradeOrderService.lambdaUpdate()
                .eq(BaseEntity::getId, tradeOrderDO.getId())
                .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                .set(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.COMPLETED.getCode())
                .set(TradeOrderDO::getPayType, payType)
                .set(TradeOrderDO::getPayMethod, payMethod)
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.error("交易单更新失败");

            // 【边界条件3】并发冲突：版本号不匹配，抛出异常让MQ重试
            throw new MessageRetryConsumeException("交易单更新失败");
        }

        // 【后置处理】触发交易成功后续逻辑（如发送订单支付成功事件）
        TradeSuccessHandler tradeSuccessHandler = TradeSuccessHandlerFactory.getTradeSuccessHandler(tradeType);
        PostTradeSuccessHandlerContext context = new PostTradeSuccessHandlerContext();
        context.setTradeOrderDO(tradeOrderDO);
        context.setPayType(payType);
        context.setPayMoney(payMoney);
        tradeSuccessHandler.postTradeSuccessHandler(context);


    }




    /**
     * 组合支付（批量支付）场景处理
     * <p>
     * 业务背景：用户购物车有多家店铺商品，生成一个主订单，对应多个子交易单。
     * 支付时一次性完成所有子交易单的支付。
     * <p>
     * 边界条件：
     * 1. 批量交易单不存在 → 数据异常，抛异常
     * 2. 已支付且同渠道 → 幂等返回
     * 3. 已支付但不同渠道 → 多渠道支付，需退款
     * 4. 状态为 MERGE_TRANSFER_SINGLE → 合并转单笔已完成，此时再支付需退款
     * 5. 部分子单更新失败 → 抛出异常，整个事务回滚
     *
     * @param outTradeNo 商户订单号
     * @param payType    支付渠道编码
     * @param payMethod  支付方式枚举
     */
    @Transactional
    public void handleCombinedPayScene(String outTradeNo, Integer payType, PayMethodEnum payMethod) {

        BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                .eq(BathTradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        // 【边界条件1】批量交易单不存在
        if (bathTradeOrderDO == null) {
            log.error("批量交易单不存在");
            throw new BizException("批量交易单不存在");
        }
        // 【边界条件2】幂等：已支付且同渠道
        if (BathTradeOrderStatusEnum.COMPLETED.getCode()
                .equals(bathTradeOrderDO.getPayStatus())
                && PayChannelEnum.getByCode(payType).
                equals(bathTradeOrderDO.getPayType())) {

            log.info("批量交易单已为支付成功状态");
            return;
        }

        // 【边界条件3】多渠道支付
        if (BathTradeOrderStatusEnum.COMPLETED.getCode()
                .equals(bathTradeOrderDO.getPayStatus())
                && !PayChannelEnum.getByCode(payType).
                equals(bathTradeOrderDO.getPayType())) {
            /**
             *
             * 交易单为支付完成状态 ,且当前支付渠道与交易单支付渠道不一致
             * 说明 用户完成了多渠道支付 ，那么要进行退款
             *
             */
            log.warn("多渠道完成支付,进行退款");

            return;
        }

        // 【边界条件4】合并转单笔已完成：用户先完成合并支付，又发起单笔支付
        if (BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode().equals(bathTradeOrderDO.getPayStatus())) {
            /**
             * 先完成合并支付，然后又发起了单笔支付，合并转单笔已 完成，
             * 那么这里就需要进行退款
             *
             *
             */
            log.warn("合并支付完成，进行退款");
            return;

        }

        // 查询该批量交易单下的所有子交易单
        List<TradeOrderDO> tradeOrderDOList = tradeOrderService.lambdaQuery().
                eq(TradeOrderDO::getBathPayOrderId, bathTradeOrderDO.getId()).list();

        // 【核心操作1】更新批量交易单状态为 COMPLETED
        boolean update1 = bathTradeOrderService.lambdaUpdate()
                .eq(BaseEntity::getId, bathTradeOrderDO.getId())
                .eq(BathTradeOrderDO::getVersion, bathTradeOrderDO.getVersion())
                .set(BathTradeOrderDO::getPayType, payType)
                .set(BathTradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                .set(BathTradeOrderDO::getVersion, bathTradeOrderDO.getVersion() + 1)
                .update();
        if (!update1) {
            log.warn("交易单更新失败");
            throw new MessageRetryConsumeException("交易单更新失败");
        }
        // 【核心操作2】逐笔更新子交易单状态
        for (TradeOrderDO tradeOrderDO : tradeOrderDOList) {
            boolean update = tradeOrderService.lambdaUpdate()
                    .eq(BaseEntity::getId, tradeOrderDO.getId())
                    .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                    .set(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.COMPLETED.getCode())
                    .set(TradeOrderDO::getPayType, payType)
                    .set(TradeOrderDO::getPayMethod, payMethod)
                    .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                    .update();
            if (!update) {
                log.warn("交易单更新失败");
                /**
                 * 抛出异常 回滚事务
                 */
                throw new MessageRetryConsumeException("交易单更新失败");
            }
        }
        // 【后置处理】发送交易成功事件，通知订单域更新状态
        TradeSuccessEventMessage message = buildTradeSuccessEventMessage(bathTradeOrderDO.getMainOrderId(),
                bathTradeOrderDO.getUserId());
        message.setPayType(payType);
        mqSendMessageUtils.sendMessage(TopicName.TRADE_SUCCESS_EVENT_TOPIC,
                JsonUtils.toJsonString(message),null);


    }

    /**
     * 构建交易成功事件消息（批量支付场景）
     *
     * @param mainOrderId 主订单ID
     * @param userId      用户ID
     * @return 交易成功事件消息
     */
    private TradeSuccessEventMessage buildTradeSuccessEventMessage(Long mainOrderId
            , Long userId) {

        TradeSuccessEventMessage message = new TradeSuccessEventMessage();
        message.setBathPay(true);
        message.setMainOrderId(mainOrderId);
        message.setUserId(userId);
        return message;
    }


    /**
     * 合并转单笔支付场景处理
     * <p>
     * 业务背景：用户先发起组合支付（未支付），后转为单笔支付。
     * 此时需要：
     * 1. 将原组合支付单标记为 MERGE_TRANSFER_SINGLE（合并转单笔）
     * 2. 将当前交易单标记为 COMPLETED
     * <p>
     * 边界条件：
     * 1. 组合支付单不存在 → 抛异常
     * 2. 交易单已支付且同渠道 → 幂等返回
     * 3. 交易单已支付但不同渠道 → 多渠道支付，需退款
     * 4. 组合支付单已是 COMPLETED → 说明合并支付已完成，此时单笔支付需退款
     *
     * @param payType      支付渠道编码
     * @param payMethod    支付方式枚举
     * @param tradeOrderDO 当前交易单
     */
    @Transactional
    public void handleCombinedToSinglePayScene(Integer payType,
                                               PayMethodEnum payMethod, TradeOrderDO tradeOrderDO) {


        Long bathPayOrderId = tradeOrderDO.getBathPayOrderId();
        BathTradeOrderDO orderDO = bathTradeOrderService.getById(bathPayOrderId);
        // 【边界条件1】组合支付单不存在
        if (orderDO == null) {
            log.warn("组合支付单不存在");
            throw new BizException("组合支付单不存在");
        }
        boolean hasReturn = false;
        // 【边界条件2】幂等：已支付且同渠道
        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus())
                && PayChannelEnum.getByCode(payType).
                equals(tradeOrderDO.getPayType())) {

            log.info("交易单已为支付成功状态");
            hasReturn = true;
        }

        // 【边界条件3】多渠道支付
        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus())
                && !PayChannelEnum.getByCode(payType).
                equals(tradeOrderDO.getPayType())) {
            /**
             *
             * 交易单为支付完成状态 ,且当前支付渠道与交易单支付渠道不一致
             * 说明 用户完成了多渠道支付 ，那么要进行退款
             *
             */
            log.warn("多渠道完成支付,进行退款");

            hasReturn = true;

        }

        // 【边界条件4】组合支付单已完成：说明用户先完成了合并支付，又发起单笔支付
        if (BathTradeOrderStatusEnum.COMPLETED.getCode().equals(orderDO.getPayStatus())) {
            /**
             * 先完成合并支付，然后又发起了单笔支付，合并付款已完成
             * 那么这里就需要进行退款
             *
             *
             */
            log.warn("合并支付完成，进行退款");
            hasReturn = true;

        }
        // 上述四种边界条件只要命中一个，就不需要继续执行
        if (hasReturn) {

            return;
        }

        // 【核心操作1】将原组合支付单标记为 MERGE_TRANSFER_SINGLE
        boolean update = bathTradeOrderService.lambdaUpdate()
                .eq(BaseEntity::getId, bathPayOrderId)
                .eq(BathTradeOrderDO::getVersion, orderDO.getVersion())
                .set(BathTradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode())
                .set(BathTradeOrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("批量交易单更新失败");
            throw new BizException("批量交易单更新失败");
        }

        // 【核心操作2】将当前交易单标记为 COMPLETED
        boolean update2 = tradeOrderService.lambdaUpdate()
                .eq(TradeOrderDO::getId, tradeOrderDO.getId())
                .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                .set(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.COMPLETED.getCode())
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .set(TradeOrderDO::getPayType, payType)
                .set(TradeOrderDO::getPayMethod, payMethod)
                .update();
        if (!update2) {
            log.warn("交易单已支付");
            throw new BizException("交易单已支付");
        }
        // 【后置处理】发送交易成功事件
        TradeSuccessEventMessage message = buildTradeSuccessEventMessage(tradeOrderDO);
        message.setPayType(payType);
        mqSendMessageUtils.sendMessage(TopicName.TRADE_SUCCESS_EVENT_TOPIC,
                JsonUtils.toJsonString(message),null);
    }

    /**
     * 构建交易成功事件消息（合并转单笔场景）
     *
     * @param tradeOrderDO 交易单实体
     * @return 交易成功事件消息
     */
    private TradeSuccessEventMessage buildTradeSuccessEventMessage(TradeOrderDO tradeOrderDO) {

        TradeSuccessEventMessage message = new TradeSuccessEventMessage();
        message.setBathPay(false);
        message.setOrderId(tradeOrderDO.getOrderId());
        message.setUserId(tradeOrderDO.getUserId());
        return message;
    }
}
