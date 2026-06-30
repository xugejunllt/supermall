package com.lanf.pay.mq.listener.event;

import com.lanf.api.order.mq.message.SignOrderMessage;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.pay.model.entity.ClearingDetailDO;
import com.lanf.api.pay.model.enums.ClearingStatusEnum;
import com.lanf.api.pay.model.vo.RecipientTypeEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.service.clearing.ClearingDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 订单签收时 进行新增结算单
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderTopicWithTag.ORDER_EVENT_TOPIC,
        consumerGroup = PayMqGroupName.SIGN_ORDER_EVENT_FINANCE_GROUP,
selectorExpression = OrderTopicWithTag.TAG_RECEIVED)

public class SignOrderEventAddClearingDetailListener implements RocketMQListener<SignOrderMessage> {

    @Autowired
    private ClearingDetailService clearingDetailService;
    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private static final BigDecimal ZERO_POINT_ZERO_ONE = new BigDecimal("0.01");

    /**
     * 平台费率 5%
     */
    private static final BigDecimal rate = new BigDecimal(50);

    @Override
    public void onMessage(SignOrderMessage message) {

        log.info("监听到订单已签收消息:{}", message);
        Long orderId = message.getOrderId();
        Date signTime = message.getSignTime();
        BigDecimal payMoney = message.getPayMoney();
        Integer afterSaleDays = message.getAfterSaleDays();
        Long merchantId = message.getTenantId();
        Date afterSaleExpireTime = new Date(signTime.getTime() + afterSaleDays * 24 * 60 * 60 * 1000);

        //商家收入
        BigDecimal merchantIncomeMoney = calculateMerchantIncome(payMoney, rate);
        ClearingDetailDO merchantLiquidationFlowDO = new ClearingDetailDO();
        merchantLiquidationFlowDO.setOrderId(orderId);
        merchantLiquidationFlowDO.setPayMoney(payMoney);
        merchantLiquidationFlowDO.setTenantId(merchantId);
        merchantLiquidationFlowDO.setAfterSaleExpireTime(afterSaleExpireTime);
        merchantLiquidationFlowDO.setStatus(ClearingStatusEnum.WAIT_CLEARING);
        merchantLiquidationFlowDO.setRecipientType(RecipientTypeEnum.MERCHANT);
        merchantLiquidationFlowDO.setIncomeMoney(merchantIncomeMoney);
        merchantLiquidationFlowDO.setRate( rate);

        try {
            clearingDetailService.save(merchantLiquidationFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("订单已创建结算单");
        }

    }
    /**
     * 计算商家收入金额
     * 公式: 商家收入 = 支付金额 × (100 - 费率) × 0.01
     * 例如: 支付100元, 费率5%, 商家收入 = 100 × (100-5) × 0.01 = 95元
     *
     * @param payMoney 支付金额
     * @param rate 费率百分比
     * @return 商家收入金额
     */
    private BigDecimal calculateMerchantIncome(BigDecimal payMoney, BigDecimal rate) {
        BigDecimal rateFactor = BigDecimalUtil.subtract(HUNDRED, rate);
        return BigDecimalUtil.multiply(payMoney, rateFactor).multiply(ZERO_POINT_ZERO_ONE);
    }

}