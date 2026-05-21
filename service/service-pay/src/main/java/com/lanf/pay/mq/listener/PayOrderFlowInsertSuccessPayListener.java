package com.lanf.pay.mq.listener;


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
import com.lanf.pay.model.entity.PayOrderFlowDO;
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
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.OrderPayInfo;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


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
    private RocketMqClient rocketMqClient;

    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private IPayOrderFlowService payOrderFlowService;

    @Override
    public void onMessage(PayOrderFlowInsertSuccessMessage message) {

        log.info("插入支付流水成功:[{}]", JsonUtils.toJsonString(message));
        String outTradeNo = message.getOutTradeNo();
        Boolean bathPay = message.getBathPay();
        Integer payType = message.getPayType();
        BigDecimal payMoney = message.getPayMoney();
        PayMethodEnum payMethod = message.getPayMethod();
        PaySceneEnum payScene = getPayScene(outTradeNo, bathPay);
        if (PaySceneEnum.SINGLE_ORDER_SINGLE_PAY.equals(payScene)) {

            handleSinglePayScene(outTradeNo, payType, message.getTradePurpose(), payMethod, payMoney);
        }
        if (PaySceneEnum.COMBINED_PAY.equals(payScene)) {

            handleCombinedPayScene(outTradeNo, payType, payMethod);

        }

        if (PaySceneEnum.COMBINED_TO_SINGLE_PAY.equals(payScene)) {

            handleCombinedToSinglePayScene(outTradeNo, payType, payMethod);
        }

    }

    /**
     * 获取支付场景
     */
    private PaySceneEnum getPayScene(String outTradeNo, Boolean bathPay) {

        if (!bathPay) {
            TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery()
                    .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();
            if (tradeOrderDO == null) {
                log.error("交易单不存在outTradeNo:[{}]", outTradeNo);
                throw new BizException("交易单不存在");
            }
            if (tradeOrderDO.getBathPay() == 0) {
                return PaySceneEnum.SINGLE_ORDER_SINGLE_PAY;
            }
            return PaySceneEnum.COMBINED_TO_SINGLE_PAY;
        }
        log.info("组合付款");

        return PaySceneEnum.COMBINED_PAY;

    }

    @Transactional
    public void handleSinglePayScene(String outTradeNo, Integer payType,
                                     TradePurposeEnum tradeType, PayMethodEnum payMethod, BigDecimal payMoney) {


        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (tradeOrderDO == null) {
            log.error("交易单不存在outTradeNo:[{}]", outTradeNo);

            throw new BizException("交易单不存在");
        }


        boolean alreadyPaid = isAlreadyPaid(outTradeNo, payType);
        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus()) && !alreadyPaid) {
            /**
             * 退款 被其他支付渠道支付过了
             */

        }

        if (BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode().
                equals(tradeOrderDO.getPayStatus())) {
            /**
             * 待处理--合并转换
             */

        }
        if (TradeOrderStatusEnum.COMPLETED.getCode().equals(tradeOrderDO.getPayStatus())){
            log.info("交易单支付完成");
            return;
        }

        if (TradeOrderStatusEnum.PENDING.getCode().equals(tradeOrderDO.getPayStatus())) {


            boolean update = tradeOrderService.lambdaUpdate()
                    .eq(BaseEntity::getId, tradeOrderDO.getId())
                    .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                    .eq(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.PENDING.getCode())
                    .set(TradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                    .set(TradeOrderDO::getPayType, payType)
                    .set(TradeOrderDO::getPayMethod, payMethod)
                    .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                    .update();
            if (!update) {
                log.error("交易单更新失败");

                throw new BizException("交易单更新失败");
            }


            /**
             * 交易成功 后置处理
             */
            TradeSuccessHandler tradeSuccessHandler = TradeSuccessHandlerFactory.getTradeSuccessHandler(tradeType);
            PostTradeSuccessHandlerContext context = new PostTradeSuccessHandlerContext();
            context.setTradeOrderDO(tradeOrderDO);
            context.setPayType(payType);
            context.setPayMoney(payMoney);
            tradeSuccessHandler.postTradeSuccessHandler(context);

        }


    }


    /**
     * 是否已经支付过 以唯一支付流水为准
     */
    private boolean isAlreadyPaid(String outTradeNo, Integer payType) {

        PayOrderFlowDO flowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                .eq(PayOrderFlowDO::getPayType, payType).one();
        return flowDO != null;
    }

    @Transactional
    public void handleCombinedPayScene(String outTradeNo, Integer payType, PayMethodEnum payMethod) {

        BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                .eq(BathTradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (bathTradeOrderDO == null) {
            log.error("批量交易单不存在");
            throw new BizException("批量交易单不存在");
        }

        boolean alreadyPaid = isAlreadyPaid(outTradeNo, payType);

        if (BathTradeOrderStatusEnum.COMPLETED.getCode()
                .equals(bathTradeOrderDO.getPayStatus()) && !alreadyPaid) {

            /**
             * 退款
             */

        }

        Integer payStatus = bathTradeOrderDO.getPayStatus();

        if (BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode().equals(payStatus)) {
            /**
             * 合并单已转单笔 进行退款
             */

        }
        if (BathTradeOrderStatusEnum.PENDING.getCode().equals(payStatus)) {

            List<TradeOrderDO> tradeOrderDOList = tradeOrderService.lambdaQuery().
                    eq(TradeOrderDO::getBathPayOrderId, bathTradeOrderDO.getId()).list();

            boolean update1 = bathTradeOrderService.lambdaUpdate()
                    .eq(BaseEntity::getId, bathTradeOrderDO.getId())
                    .eq(BathTradeOrderDO::getVersion, bathTradeOrderDO.getVersion())
                    .set(BathTradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                    .set(BathTradeOrderDO::getVersion, bathTradeOrderDO.getVersion() + 1)
                    .update();
            if (!update1) {
                log.warn("交易单更新失败");
                throw new BizException("交易单更新失败");
            }
            /**
             *
             * 这里修改一下 要么全部成功 要么全部失败
             *
             */
            for (TradeOrderDO tradeOrderDO : tradeOrderDOList) {
                boolean update = tradeOrderService.lambdaUpdate()
                        .eq(BaseEntity::getId, tradeOrderDO.getId())
                        .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                        .eq(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.PENDING.getCode())
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
                    throw new BizException("交易单更新失败");
                }
            }
            /**
             * 发送mq
             */

            TradeSuccessEventMessage message = buildTradeSuccessEventMessage(bathTradeOrderDO.getMainOrderId(), tradeOrderDOList);

            rocketMqClient.sendMessage(TopicName.TRADE_SUCCESS_EVENT_TOPIC, JsonUtils.toJsonString(message));

        }


    }

    private TradeSuccessEventMessage buildTradeSuccessEventMessage( Long mainOrderId
            , List<TradeOrderDO> tradeOrderDOList) {
        List<OrderPayInfo> orderPayInfoList = new ArrayList<>();
        for (TradeOrderDO tradeOrderDO : tradeOrderDOList) {

            OrderPayInfo orderPayInfo = new OrderPayInfo();
            orderPayInfo.setOrderId(tradeOrderDO.getOrderId());
            orderPayInfoList.add(orderPayInfo);

        }
        TradeSuccessEventMessage message = new TradeSuccessEventMessage();
        message.setBathPay(true);
        message.setMainOrderId(mainOrderId);
        message.setOrderPayInfoList(orderPayInfoList);
        message.setUserId(orderPayInfoList.get(0).getOrderId());
        return message;
    }


    @Transactional
    public void handleCombinedToSinglePayScene(String outTradeNo, Integer payType, PayMethodEnum payMethod) {


        TradeOrderDO tradeOrderDO = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (tradeOrderDO == null) {
            log.warn("交易单不存在");
            throw new BizException("交易单不存在");
        }

        Long bathPayOrderId = tradeOrderDO.getBathPayOrderId();
        BathTradeOrderDO orderDO = bathTradeOrderService.getById(bathPayOrderId);
        if (orderDO == null) {
            log.warn("组合支付单不存在");
            throw new BizException("组合支付单不存在");
        }

        boolean alreadyPaid = isAlreadyPaid(outTradeNo, payType);

        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus()) && !alreadyPaid) {
            /**
             * 退款
             */
            return;
        }


        if (BathTradeOrderStatusEnum.PENDING.
                getCode().equals(orderDO.getPayStatus())) {
            log.info("更新批量交易单状态");

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

        }
        boolean update = tradeOrderService.lambdaUpdate()
                .eq(TradeOrderDO::getId, tradeOrderDO.getId())
                .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                .set(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.COMPLETED.getCode())
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .set(TradeOrderDO::getPayType, payType)
                .set(TradeOrderDO::getPayMethod, payMethod)
                .update();
        if (!update) {
            log.warn("交易单已支付");
            throw new BizException("交易单已支付");
        }
        /**
         * 发送mq
         */
        TradeSuccessEventMessage message = buildTradeSuccessEventMessage( tradeOrderDO);
        rocketMqClient.sendMessage(TopicName.TRADE_SUCCESS_EVENT_TOPIC, JsonUtils.toJsonString(message));
    }

    private TradeSuccessEventMessage buildTradeSuccessEventMessage(TradeOrderDO tradeOrderDO) {
        List<OrderPayInfo> orderPayInfoList = new ArrayList<>();
        OrderPayInfo orderPayInfo = new OrderPayInfo();
        orderPayInfo.setOrderId(tradeOrderDO.getOrderId());
        orderPayInfoList.add(orderPayInfo);
        TradeSuccessEventMessage message = new TradeSuccessEventMessage();
        message.setBathPay(false);
        message.setMainOrderId(tradeOrderDO.getOrderId());
        message.setOrderPayInfoList(orderPayInfoList);
        message.setUserId(tradeOrderDO.getUserId());
        return message;
    }
}
